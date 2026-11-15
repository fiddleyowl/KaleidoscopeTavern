#!/usr/bin/env python3
"""Check the 26.1.2 port against the pinned upstream content commit (stdlib only)."""
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = 'c4ec1880bd44cf3139d3ba744ab30bb379cf1416'
NAMESPACE = 'kaleidoscope_tavern'
JAVA = Path('src/main/java/com/github/ysbbbbbb/kaleidoscopetavern')
RESOURCE_ROOTS = [ROOT / 'src/main/resources', ROOT / 'src/generated/resources']
errors = []


def git(*args):
    return subprocess.check_output(['git', '-C', str(ROOT), *args])


def check(condition, message):
    if not condition:
        errors.append(message)


def resource(path):
    matches = [base / path for base in RESOURCE_ROOTS if (base / path).is_file()]
    check(len(matches) <= 1, f'Duplicate resource: {path}')
    return matches[0] if matches else None


def normalize(value):
    """Equivalent recipe representations before and after vanilla's codec changes."""
    if isinstance(value, list):
        return [normalize(v) for v in value]
    if isinstance(value, dict):
        if set(value) == {'id'}:
            return normalize(value['id'])
        if set(value) == {'item'}:
            return normalize(value['item'])
        if set(value) == {'tag'}:
            return '#' + normalize(value['tag'])
        result = {k: normalize(v) for k, v in value.items()}
        if 'id' in result and result.get('count') == 1:
            result.pop('count')
        if 'item' in result and 'count' in result:
            result['id'] = result.pop('item')
        for key in ['category', 'show_notification', 'group']:
            result.pop(key, None)
        if result.get('ingredients') == []:
            result.pop('ingredients')
        if result.get('fluid_amount') == 125:
            result.pop('fluid_amount')
        if result.get('unit_time') == 2400:
            result.pop('unit_time')
        if set(result) == {'id'}:
            return result['id']
        return result
    if isinstance(value, str):
        return {'minecraft:chain': 'minecraft:iron_chain', 'minecraft:grass': 'minecraft:short_grass'}.get(value, value.replace('forge:', 'c:'))
    return value


paths = git('ls-tree', '-r', '--name-only', UPSTREAM).decode().splitlines()
for file, register in [('ModBlocks.java', 'BLOCKS'), ('ModItems.java', 'ITEMS'), ('ModEffects.java', 'EFFECTS')]:
    path = JAVA / 'init' / file
    pattern = rf'\b{register}\.register\("([^"]+)"'
    expected = set(re.findall(pattern, git('show', f'{UPSTREAM}:{path}').decode()))
    actual = set(re.findall(pattern, (ROOT / path).read_text()))
    check(actual == expected, f'{register}: missing {expected - actual}, extra {actual - expected}')
    print(f'{register}: {len(actual)} IDs match upstream')

# Binary assets remain byte-identical; all original translation keys and values are retained.
binaries = 0
for path in paths:
    if path.startswith('src/main/resources/assets/') and (path.endswith(('.png', '.ogg', '.nbt', '.mcmeta')) or '/lang/' in path):
        relative = path.removeprefix('src/main/resources/')
        current = resource(relative)
        check(current is not None, f'Missing upstream asset: {relative}')
        if current:
            check(all(json.loads(current.read_bytes()).get(k) == v for k, v in json.loads(git('show', f'{UPSTREAM}:{path}')).items()) if '/lang/' in relative else current.read_bytes() == git('show', f'{UPSTREAM}:{path}'), f'Changed upstream asset: {relative}')
        binaries += 1
print(f'Artwork, sounds, structures and translations: {binaries} upstream assets checked')

recipe_count = 0
for path in paths:
    if path.startswith('src/generated/resources/data/kaleidoscope_tavern/recipes/') and path.endswith('.json'):
        relative = path.removeprefix('src/generated/resources/').replace('/recipes/', '/recipe/').replace('_from_grass.json', '_from_short_grass.json')
        current = resource(relative)
        check(current is not None, f'Missing upstream recipe: {relative}')
        if current:
            before = normalize(json.loads(git('show', f'{UPSTREAM}:{path}')))
            after = normalize(json.loads(current.read_bytes()))
            check(before == after, f'Recipe differs: {relative}: {before} != {after}')
        recipe_count += 1
print(f'Recipes: {recipe_count} upstream recipes checked')

# The native synced registry uses level keys; the old reload listener used list positions.
effect_count = 0
for path in paths:
    if '/drink_effect/' not in path or not path.endswith('.json'):
        continue
    before = json.loads(git('show', f'{UPSTREAM}:{path}'))
    relative = f'data/{NAMESPACE}/{NAMESPACE}/drink_effect/{Path(path).name}'
    current = resource(relative)
    check(current is not None, f'Missing drink data: {relative}')
    if current:
        after = json.loads(current.read_bytes())
        levels = before['effects']
        expected = {str(i): v for i, v in enumerate(levels, start=1 if len(levels) > 1 else 0)}
        check(after['effects'] == expected, f'Drink effects differ: {relative}')
    effect_count += 1
print(f'Drink effects: {effect_count} upstream definitions checked')

# Resolve all local model/texture references and all native item entry points.
model_count = 0
for base in RESOURCE_ROOTS:
    for path in (base / f'assets/{NAMESPACE}').rglob('*.json'):
        value = json.loads(path.read_bytes())
        relative = path.relative_to(base)
        if 'models' not in relative.parts and 'items' not in relative.parts and 'blockstates' not in relative.parts:
            continue
        model_count += 1
        def walk(node):
            if isinstance(node, dict):
                for key, item in node.items():
                    if key in ['parent', 'model'] and isinstance(item, str) and item.startswith(NAMESPACE + ':'):
                        check(resource(f'assets/{NAMESPACE}/models/{item.split(":", 1)[1]}.json') is not None,
                              f'{relative}: unresolved model {item}')
                    elif key == 'textures' and isinstance(item, dict):
                        for texture in item.values():
                            if texture.startswith(NAMESPACE + ':'):
                                check(resource(f'assets/{NAMESPACE}/textures/{texture.split(":", 1)[1]}.png') is not None,
                                      f'{relative}: unresolved texture {texture}')
                    walk(item)
            elif isinstance(node, list):
                for item in node:
                    walk(item)
        walk(value)
for item in re.findall(r'\bITEMS\.register\("([^"]+)"', (ROOT / JAVA / 'init/ModItems.java').read_text()):
    check(resource(f'assets/{NAMESPACE}/items/{item}.json') is not None, f'Missing native item definition: {item}')
print(f'Models, blockstates and native item definitions: {model_count} files checked')

if errors:
    for error in errors:
        print('FAIL:', error)
    raise SystemExit(f'{len(errors)} checks failed')
print('All port content checks passed')
