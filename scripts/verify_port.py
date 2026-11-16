#!/usr/bin/env python3
"""Check the 26.1.2 port against the pinned upstream content commit (stdlib only)."""
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = 'c4ec1880bd44cf3139d3ba744ab30bb379cf1416'
UPSTREAM_NEOFORGE = 'a1afba34981a00e89d57130d3dc8f1e64f1820a2'
UPSTREAM_26 = '9b8f165a4641e738e694641dd81240a0206573e1'
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
        if value.startswith('#'):
            return '#' + normalize(value[1:])
        if value.startswith('forge:'):
            return 'c:' + value.removeprefix('forge:')
        return {'minecraft:chain': 'minecraft:iron_chain', 'minecraft:grass': 'minecraft:short_grass'}.get(value, value)
    return value


paths = git('ls-tree', '-r', '--name-only', UPSTREAM).decode().splitlines()
for file, register in [
    ('ModBlocks.java', 'BLOCKS'), ('ModItems.java', 'ITEMS'), ('ModEffects.java', 'EFFECTS'),
    ('ModBlocks.java', 'BLOCK_ENTITIES'), ('ModEntities.java', 'ENTITY_TYPES'),
    ('ModParticles.java', 'PARTICLES'), ('ModCreativeTabs.java', 'TABS'),
    ('ModRecipes.java', 'RECIPE_SERIALIZERS'), ('ModTreeDecoratorTypes.java', 'TREE_DECORATOR_TYPES'),
]:
    path = JAVA / 'init' / file
    pattern = rf'\b{register}\.register\(\s*"([^"]+)"'
    expected = set(re.findall(pattern, git('show', f'{UPSTREAM}:{path}').decode()))
    actual = set(re.findall(pattern, (ROOT / path).read_text()))
    check(actual == expected, f'{register}: missing {expected - actual}, extra {actual - expected}')
    print(f'{register}: {len(actual)} IDs match upstream')

# These upstream registries used helper methods or manual RegisterEvent callbacks.
old_fluids = git('show', f'{UPSTREAM}:{JAVA}/init/ModFluids.java').decode()
fluid_ids = dict(re.findall(r'(\w+_ID)\s*=\s*modLoc\("([^"]+)"', old_fluids))
expected_types = {fluid_ids[key] for key in re.findall(r'\w+_TYPE\s*=\s*RegistryObject\.create\((\w+_ID),', old_fluids)}
new_fluids = (ROOT / JAVA / 'init/ModFluids.java').read_text()
for register, expected in [('FLUIDS', set(fluid_ids.values())), ('FLUID_TYPES', expected_types)]:
    actual = set(re.findall(rf'\b{register}\.register\("([^"]+)"', new_fluids))
    check(actual == expected, f'{register}: missing {expected - actual}, extra {actual - expected}')
    print(f'{register}: {len(actual)} IDs match upstream')
for file, pattern in [('init/ModSounds.java', r'registerSound\("([^"]+)"'),
                      ('init/tag/TagMod.java', r'(?:blockTag|itemTag|entityTag|damageTypeTag)\("([^"]+)"')]:
    expected = set(re.findall(pattern, git('show', f'{UPSTREAM}:{JAVA / file}').decode()))
    actual = set(re.findall(pattern, (ROOT / JAVA / file).read_text()))
    check(actual == expected, f'{file}: missing {expected - actual}, extra {actual - expected}')
    print(f'{file}: {len(actual)} names match upstream')
old_recipes = git('show', f'{UPSTREAM}:{JAVA}/init/ModRecipes.java').decode()
expected_types = set(re.findall(r'RecipeType\.simple\(KaleidoscopeTavern\.modLoc\("([^"]+)"', old_recipes))
actual_types = set(re.findall(r'RECIPE_TYPES\.register\("([^"]+)"', (ROOT / JAVA / 'init/ModRecipes.java').read_text()))
check(actual_types == expected_types, f'Recipe type IDs changed: {expected_types} != {actual_types}')

# Components and existing payloads are public identifiers from the NeoForge branches.
component_path = JAVA / 'init/ModDataComponents.java'
component_pattern = r'DATA_COMPONENT_TYPES\.register\("([^"]+)"'
expected_components = set(re.findall(component_pattern, git('show', f'{UPSTREAM_NEOFORGE}:{component_path}').decode()))
actual_components = set(re.findall(component_pattern, (ROOT / component_path).read_text()))
check(actual_components == expected_components, f'Data component IDs changed: {expected_components} != {actual_components}')
for name in ['TextOpenS2CMessage.java', 'TextUpdateC2SMessage.java']:
    path = JAVA / 'network/message' / name
    pattern = r'modLoc\("([^"]+)"'
    check(set(re.findall(pattern, git('show', f'{UPSTREAM_26}:{path}').decode())) ==
          set(re.findall(pattern, (ROOT / path).read_text())), f'26.1.2 payload ID changed: {name}')
for name in ['BREW_LEVEL', 'SHAKER_RESULT', 'SIGNATURE_COCKTAIL_EFFECTS', 'SIGNATURE_COCKTAIL_COLOR']:
    check(name in (ROOT / component_path).read_text(), f'Missing public component field: {name}')
for path in (ROOT / JAVA).rglob('*.java'):
    package = re.search(r'^package ([\w.]+);', path.read_text(), re.MULTILINE)
    check(package is not None and package[1] == '.'.join(path.parent.relative_to(ROOT / 'src/main/java').parts),
          f'Java package/path mismatch: {path.relative_to(ROOT)}')
check('MOD_ID = "kaleidoscope_tavern"' in (ROOT / JAVA / 'KaleidoscopeTavern.java').read_text(), 'Mod namespace changed')
print('Mod namespace, Java package paths, data components and 26.1.2 payload IDs checked')
config_path = JAVA / 'config/GeneralConfig.java'
config_pattern = r'builder\.(?:push|define)\("([^"]+)"'
check(re.findall(config_pattern, git('show', f'{UPSTREAM}:{config_path}').decode()) ==
      re.findall(config_pattern, (ROOT / config_path).read_text()), 'Configuration key names or sections changed')

# Normalize vanilla directory names only; the rest of a path is a public resource ID.
def current_resource_path(path):
    for prefix in ['src/main/resources/', 'src/generated/resources/']:
        if path.startswith(prefix):
            path = path.removeprefix(prefix)
            break
    parts = path.split('/')
    if parts[0] == 'data' and len(parts) > 3:
        parts[2] = {'recipes': 'recipe', 'loot_tables': 'loot_table', 'advancements': 'advancement',
                    'structures': 'structure'}.get(parts[2], parts[2])
        if parts[2] == 'tags':
            parts[3] = {'blocks': 'block', 'items': 'item', 'entity_types': 'entity_type',
                        'fluids': 'fluid'}.get(parts[3], parts[3])
        if parts[2:4] == ['datamap', 'drink_effect']:
            parts[2:4] = [NAMESPACE, 'drink_effect']
    if path == 'META-INF/mods.toml':
        return 'META-INF/neoforge.mods.toml'
    return '/'.join(parts)

checked = 0
for path in paths:
    if not path.startswith(('src/main/resources/', 'src/generated/resources/')) or '/.cache/' in path:
        continue
    target = current_resource_path(path)
    check(resource(target) is not None, f'Missing public resource from {UPSTREAM[:7]}: {target}')
    checked += 1
print(f'Public resource paths from {UPSTREAM[:7]}: {checked} checked')

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

# Translation keys referenced by merged Java sources must exist in the retained language files.
translation_keys = set()
for path in (ROOT / JAVA).rglob('*.java'):
    translation_keys.update(re.findall(r'"([\w.]*kaleidoscope_tavern\.[\w.%]+)"', path.read_text()))
for locale in ['en_us', 'zh_cn']:
    translations = json.loads(resource(f'assets/{NAMESPACE}/lang/{locale}.json').read_bytes())
    for key in translation_keys:
        if '%' in key:
            pattern = re.escape(key).replace('%s', r'[a-z_]+').replace('%d', r'\d+')
            check(any(re.fullmatch(pattern, candidate) for candidate in translations),
                  f'{locale}: unresolved translation key family {key}')
        else:
            check(key in translations, f'{locale}: unresolved translation key {key}')
print(f'Java translation references: {len(translation_keys)} keys and families checked in en_us and zh_cn')

recipe_count = 0
for path in paths:
    if path.startswith('src/generated/resources/data/kaleidoscope_tavern/recipes/') and path.endswith('.json'):
        relative = path.removeprefix('src/generated/resources/').replace('/recipes/', '/recipe/')
        current = resource(relative)
        check(current is not None, f'Missing upstream recipe: {relative}')
        if current:
            before = normalize(json.loads(git('show', f'{UPSTREAM}:{path}')))
            after = normalize(json.loads(current.read_bytes()))
            check(before == after, f'Recipe differs: {relative}: {before} != {after}')
        recipe_count += 1
print(f'Recipes: {recipe_count} upstream recipes checked')
expected_recipe_paths = {
    current_resource_path(path) for path in paths
    if path.startswith('src/generated/resources/data/kaleidoscope_tavern/recipes/') and path.endswith('.json')
}
actual_recipe_paths = {
    str(path.relative_to(base)) for base in RESOURCE_ROOTS
    for path in (base / f'data/{NAMESPACE}/recipe').rglob('*.json')
}
check(actual_recipe_paths == expected_recipe_paths,
      f'Recipe ID set differs: missing {expected_recipe_paths - actual_recipe_paths}, extra {actual_recipe_paths - expected_recipe_paths}')
expected_advancement_paths = {
    current_resource_path(path) for path in paths
    if path.startswith('src/generated/resources/data/kaleidoscope_tavern/advancements/') and path.endswith('.json')
}
actual_advancement_paths = {
    str(path.relative_to(base)) for base in RESOURCE_ROOTS
    for path in (base / f'data/{NAMESPACE}/advancement').rglob('*.json')
}
check(actual_advancement_paths == expected_advancement_paths,
      f'Advancement ID set differs: missing {expected_advancement_paths - actual_advancement_paths}, extra {actual_advancement_paths - expected_advancement_paths}')
print(f'Recipe and advancement ID sets match upstream exactly: {len(expected_recipe_paths)} recipes, {len(expected_advancement_paths)} advancements')

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
