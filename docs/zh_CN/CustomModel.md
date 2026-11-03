ArkPets附加说明文档
# 自定义模型

ArkPets 添加自定义 Spine 模型的方法。
> 注意：  
> 本文档适用于 ArkPets v3.x，不同版本的 ArkPets 的模型管理逻辑可能有较大差异。

### 前提
1. **熟悉 JSON 数据格式。**
2. 所要添加的模型的 **Spine 版本必须是 3.8**，这也是截至本文档撰写时《明日方舟》所使用的 Spine 的版本。不同版本的 Spine 之间的兼容性较差，您可以使用文本编辑器强制查看小人骨骼（.skel）文件的头部信息，以确定其 Spine 版本。
3. 所要添加的模型必须是《明日方舟》中的角色模型，如果不是，那么该模型包含的动画名称必须与《明日方舟》中的动画名称命名方式一致。关于命名方式，参见本项目 Java 源码 `cn.harryh.arkpets.animations.AnimClip` 中的 `AnimType` 枚举）。

### 步骤
下面将演示如何添加名称为 `MyModel` 的干员类型（Operator）的模型到 ArkPets 中：

1. 在程序工作目录（下简称“根目录”）中创建一个数据集文件 `models_data.json` 和一个总模型文件夹 `models`。  
   数据集文件的格式如下（标注星号 `*` 者为必需条目）：
   ```json
   // modelsdata.json 示例：
   {
       "storageDirectory": {
           // * 每种模型类型所对应的总模型文件夹名称
           "Operator": "models"
        },
       "sortTags": {
            // 每个模型标签所对应的本地化描述
            "Operator": "干员",
            "Skinned": "时装"
        },
        "gameDataVersionDescription": "xxxxx", // 游戏数据版本描述
        "gameDataServerRegion": "zh_CN", // 游戏数据服务器地区描述
        "data": {
            // * 每个模型的信息
            // ...
        },
        "arkPetsCompatibility": [2, 2, 0] // ArkPets 兼容性版本标识
   }
   ```
   > 提示：
   > 1. 也可以通过启动器 “选项” 页的模型下载或导入功能来导入 ArkModels 仓库的数据集文件和总模型文件夹。
   > 2. 总资源文件夹的名称可以是其他名称，但是需要为数据集文件中的 `storageDirectory` 字段中添加一个键值对 `"角色类型" : "总资源文件夹名"`。
   > 3. 以上示例中的注释文本（`//`）仅作说明使用，实际上正规的 JSON 语法不支持注释，所以请不要将注释写入文件。
2. 将所要添加的模型的资源文件（包括 .atlas .png .skel 文件）放入同一个文件夹（下称“单模型文件夹”）中，然后将单模型文件夹放入总资源文件夹中。
3. 在数据集文件中的 `data` 字段中，仿照其他模型对象的格式，加入你所要添加的新模型的信息，示例如下（标注星号 `*` 者为必需条目）：
   ```json
   { // data 字段中的对象示例：
       "my_model": { // * 单模型文件夹的名称
           "assetId": "build_my_model", // (此字段已弃用，可忽略)
           "type": "Operator", // * 模型类型
           "style": "BuildingDefault", // 模型子类型
           "name": "My Model", // * 角色名称
           "sortTags": [
               // 模型标签
               "Operator"
           ],
           "appellation": "My Model", // 角色代号
           "skinGroupId": "ILLUST", // 时装系列编号
           "skinGroupName": "默认服装", // 时装系列名称
           "assetList": { // * 资源文件列表
               ".atlas": "mymodel.atlas", // 对应的文件名称
               ".png": [
                   // 如果对应的文件不止一个，可使用列表来表示
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
4. 打开 ArkPets 启动器，进行模型 “重载” 后，即可载入自定义模型。若未在启动器中找到自定义模型，请检查相关操作和拼写。
