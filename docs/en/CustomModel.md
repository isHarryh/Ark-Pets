ArkPets Supplementary Documentation
# Custom Models

Guide for importing custom Spine animation models into ArkPets.
> **Note:**  
> This guide applies to ArkPets v3.x. Model schema logic varies across major releases.

### Prerequisites
1. **Familiarity with JSON structure and syntax.**
2. Imported models **must use Spine version 3.8** (matching the Arknights runtime version). Spine versions are not backward-compatible. You can verify skeleton file versions by inspecting binary header strings in `.skel` files with a plain text editor.
3. Custom models must conform to Arknights animation naming structures. For details, inspect the `AnimType` enum in `cn.harryh.arkpets.animations.AnimClip`.

### Step-by-Step Guide
This walkthrough demonstrates adding a custom operator model named `MyModel`:

1. In the application working directory (referred to below as the root directory), create a dataset metadata file `models_data.json` and a parent model directory `models`.  
   Populate `models_data.json` using the following structure (fields marked with `*` are mandatory):
   ```json
   // Example of modelsdata.json:
   {
       "storageDirectory": {
           // * Folder name mapping for each model type
           "Operator": "models"
        },
       "sortTags": {
            // The localised description corresponding to each model label
            "Operator": "Operator",
            "Skinned": "Outfit"
        },
        "gameDataVersionDescription": "xxxxx", // Game data version description
        "gameDataServerRegion": "zh_CN", // Game data server region description
        "data": {
            // * Information of each model
            // ...
        },
        "arkPetsCompatibility": [2, 2, 0] // ArkPets compatibility version identifiers
   }
   ```
   > Tips:
   > 1. Datasets and parent model directories can also be imported directly via Manage Repositories in the launcher.
   > 2. Custom directory names are permitted provided key-value mappings (e.g., `"CharacterType": "DirectoryName"`) are specified in `storageDirectory`.
   > 3. Standard JSON specs prohibit comments; remove all comments before saving.
2. Place model assets (`.atlas`, `.png`, `.skel`) into a single subfolder (e.g., `my_model`), then move this subfolder into your parent directory (`models`).
3. Add your model metadata entry under the `data` dictionary in `models_data.json` (fields marked with `*` are required):
   ```json
   { // Examples of objects in the `data` field:
       "my_model": { // * Name of the single-model folder
           "assetId": "build_my_model", // (This field has been deprecated and may be ignored)
           "type": "Operator", // * Model type
           "style": "BuildingDefault", // Model sub-type
           "name": "My Model", // * Character name
           "sortTags": [
               // Model labels
               "Operator"
           ],
           "appellation": "My Model", // Character appellation
           "skinGroupId": "ILLUST", // Outfit collection id
           "skinGroupName": "Default Outfit", // Outfit collection name
           "assetList": { // * List of resource files
               ".atlas": "mymodel.atlas", // Corresponding file name
               ".png": [
                   // If there is more than one corresponding file, you may use a list to represent them
                   "mymodel1.png",
                   "mymodel2.png"
               ],
               ".skel": "mymodel.skel"
           }
       },
       "my_next_model": {
           // ...
       }
   }
   ```
4. Open the ArkPets launcher and execute a model database Reload. If your custom model fails to appear, double-check path references and JSON syntax formatting.
