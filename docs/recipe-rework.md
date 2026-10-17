# Miner’s Delight TFC 配方重整

## 基準與範圍

- 專案：`vomiter-scp-zh/MinersDelightTFC`，`master`，`0eff6e07bb43fafaf47b3cb4bb3fc3d83dc905a2`。
- Minecraft 1.20.1、Forge 47.4.22、Java 17。
- 實際依賴：Miner’s Delight `1.20.1-1.4.5-backport`（CurseForge file 8154984）、TFC 3.2.18（6835820）、Survivor’s Delight（8998576）。
- 先套用 `MinersDelightTFC-piercing-loot-complete.patch`，再套用 recipe patch。第一份也補上在 Miner’s Delight 之後載入的宣告，讓同 ID 的資源覆寫有正確優先順序。

## Datagen 與材料規則

`MDTFCDisabledRecipesProvider` 掃描實際安裝的 Miner’s Delight JAR 中 `data/miners_delight/recipes`，以原 namespace、原 recipe ID 輸出：

```json
{"conditions":[{"type":"forge:false"}]}
```

這與 TFC 停用原版配方的方式相同。這個依賴版本共有 **53 個原始配方**，包含四個 Create 配方；全部停用。更換依賴版本後重跑 `runData`，新增的上游 JSON 也會被掃描，缺少來源目錄或找不到任何配方則明確失敗。

替代配方使用 `minersdelighttfc` namespace。配方、food data、heat data 都由 Java datagen 產生，生成結果一併包含於 patch，使用者不需要先執行 datagen 才能打包。

- 不使用原版食物或金屬材料；不使用會接受原版麵包、作物或金屬錠的廣泛 Forge 食材標籤。
- 麵包、麵糰、穀物、蔬菜、水果使用 TFC 專用標籤；具體食物及銅、金材料直接引用 TFC 註冊物件。
- 蜂蜜的黏合／甜味角色改用 `#tfc:sweetener`。TFC 3.2.18 的這個標籤預設包含 `minecraft:sugar`，屬於本次明確允許的 sweetener 替代，不再直接要求蜂蜜瓶。
- 木棒、碗、墨囊、骨粉、蜘蛛網、苔蘚方塊、線不是禁止的食物或金屬材料，保留其原本用途。切割工具的 tag 是操作工具，不是被配方消耗的金屬原料。
- 食物材料使用 `tfc:not_rotten`。組裝食物沿用 SD 的營養合成 crafting serializer；烹調食物沿用既有 cooking pot 路徑；新成品使用 dynamic food data。
- 有正面或負面特殊效果的加工食品優先使用 cooking pot。原始生肉、發光魷魚與採集型苔蘚的原生效果，不因此改成必須先煮才能取得。

## 新增的食物製作路徑

以下 ID 均省略 `minersdelighttfc:` 前綴；未註明產量者為一份。

| Recipe ID | 材料與產量 | 設計理由 |
| --- | --- | --- |
| `crafting/vegan_hamburger` | TFC 麵包、vegan patty、TFC 高麗菜、番茄、洋蔥 | 保留原本的麵包＋排餅＋三種蔬菜結構，排除原版作物與麵包。 |
| `crafting/vegan_wrap` | TFC 麵包、TFC 洋蔥、烤洞穴胡蘿蔔 | 沿用原本的麵包包裹蔬菜邏輯。 |
| `crafting/cave_hamburger` | TFC 麵包、crunchy bar 或 vegan patty、煙燻蝙蝠翅、兩份烤觸手或烤洞穴胡蘿蔔 | 保留主餡、蝙蝠肉與兩份配料；用洞穴胡蘿蔔接替原版蘑菇的植物替代選項。 |
| `crafting/insect_sandwich` | 兩份 TFC 麵包、熟節肢動物或烤蜘蛛腿、熟昆蟲肉或 weird caviar → 2 | 保留原本雙份三明治及肉／魚子醬替代。 |
| `crafting/insect_wrap` | TFC 麵包、洋蔥或生熟昆蟲肉、兩份生節肢動物或蠹魚卵 | 保留原本的昆蟲內餡組合；食物均檢查腐敗。 |
| `crafting/alt_insect_wrap` | TFC 麵包、洋蔥或生熟昆蟲肉、crunchy bar | 保留用壓縮昆蟲棒代替兩份小份內餡的原替代配方。 |
| `crafting/squid_sandwich` | TFC 麵包、完整烤魷魚 | 原本就是完整烤魷魚夾麵包，沒有用小份觸手降低成本。 |
| `crafting/bat_cookie` | 煙燻蝙蝠翅、兩份 TFC 麵糰 → 8 | 保留原本一份蝙蝠肉、兩份穀物、八片餅乾；改用已處理麵糰，取代直接使用原版小麥。 |
| `crafting/improvised_barbecue_stick` | 烤洞穴胡蘿蔔、TFC 洋蔥、煙燻蝙蝠翅、木棒 | 保留肉、兩種蔬菜與竹籤結構；洋蔥替代原版蘑菇。 |
| `crafting/tentacles_on_a_stick` | 木棒、兩份烤觸手或 TFC 熟魷魚肉 | 保留兩份熟海鮮串燒，也允許 TFC 對應食材。 |
| `cooking/copper_carrot` | 洞穴胡蘿蔔、兩份 TFC 銅雙層板、100 mB 烹調油 | 原品有中毒效果，因此改走 cooking pot；兩片雙層板共 800 metal units，對應原本八個銅錠。金屬以 nonfood 加入，不計入營養。 |
| `cooking/nutritional_bar` | 兩份 crunchy bar、TFC 穀物、sweetener → 2 | 原本四棒、兩份小麥與一瓶蜂蜜產四份；改成半批次，保留穀物與昆蟲棒比例，並在鍋的六個輸入格內完成。sweetener 每批仍需一份。保留原物品的增益與解除中毒效果。 |
| `cooking/golden_nutritional_bar` | nutritional bar、TFC 金錠 | 保留以金強化營養棒的邏輯並改走鍋具。TFC 沒有對應的原版金粒材料，八個金粒改為一個 100-unit TFC 金錠，成本略為向上取整。 |

## 既有烹調配方的修正

| Recipe ID | 實際變更 | 原因 |
| --- | --- | --- |
| `cooking/cave_soup` | 紅、棕蘑菇改為 TFC 芋頭根與香蒲根 | TFC 3.2.18 沒有相應蘑菇食物；以兩種野採根莖保留洞穴蔬菜湯的植物食材角色，避免使用原版食材。 |
| `cooking/fake_meatloaf` | 原版胡蘿蔔替代選項改為 TFC 胡蘿蔔 | 保留洞穴／普通胡蘿蔔的替代邏輯。 |
| `cooking/stuffed_squid` | 泛用 `forge:eggs` 改為 TFC 水煮蛋 | 排除原版蛋；保留蛋、穀物、蔬菜填餡的結構。 |
| `cooking/glow_ink_pasta` | 水果使用 `#tfc:foods/fruits` | 避免較廣的甜點水果 tag 接受原版莓果。 |
| `cooking/glazed_arachnid_limbs` | 蜘蛛腿由一份改為兩份，兩份水果改用 TFC 水果 tag；保留穀物、sweetener 與油 | 原版使用四份腿，但還有穀物、甜味料與兩份水果，超過鍋的六格；此版本以兩份腿保留全部材料角色，恰好六格。dynamic 營養依實際投入計算。 |
| `cooking/vegan_steak_and_potatoes` | 原版烤馬鈴薯改為 TFC 烤馬鈴薯 | 食材角色不變，只替換成 TFC 對應物。 |

其他已有的 `pasta_with_veggieballs`、`bat_rolls`、`bat_soup`、`insect_stew`、`alt_insect_stew`、`takoyaki`、`seasoned_arthropods`、`crafting/crunchy_bar`、`crafting/weird_caviar` 保留既有 TFC 配方。原版同用途 recipe 一律停用，避免較便宜或不保存營養的製作路徑並存。

## 加熱、切割、器具與加工

| Recipe ID | 變更與理由 |
| --- | --- |
| `heating/cave_carrot` | 洞穴胡蘿蔔 → 烤洞穴胡蘿蔔。 |
| `heating/bat_wing` | 蝙蝠翅 → 煙燻蝙蝠翅。 |
| `heating/spider_leg` | 蜘蛛腿 → 烤蜘蛛腿。 |
| `heating/arthropod` | 節肢動物肉 → 熟節肢動物肉；原本有效的手寫檔也併入 datagen。 |
| `heating/squid`、`heating/glow_squid_to_baked_squid` | 普通／發光完整魷魚分別產完整烤魷魚，保留上游共同熟食結果。 |
| `heating/tentacles` | 觸手 → 烤觸手。 |
| `cutting/tentacles` | 一份 TFC 生魷魚肉 → 一份 MD 觸手；補足 TFC 世界中的原料取得路徑，不增加肉量。 |
| `anvil/copper_pot` | 一片 TFC 銅雙層板、tier 1、最後三步皆 bend，產原 MD 銅鍋。延續銅製容器定位，改接 TFC 鍛造；400 units 的銅製鍋身取代原本五錠＋木鏟＋裝水容器的工作台配方。 |
| `anvil/copper_cup` | 保留先前實作：銅板鍛造 MDTFC jug-like copper cup。舊 MD 空銅杯配方停用。 |
| `crafting/string_from_gossypium` | gossypium＋TFC 紡錘 → 2 線；使用 TFC damage-inputs wrapper，損耗並返還紡錘。保留棉纖維製線原意，加入 TFC 紡織加工。 |
| `crafting/sticky_basket` | 保留原本四木棒、三蜘蛛網的形狀與產物；蜘蛛網可透過第一份 patch 的蜘蛛掉落取得。 |
| `crafting/cave_carrot_crate` | 九份未腐敗洞穴胡蘿蔔製作一個裝飾箱。**不恢復拆箱 recipe**：原方塊不保存 TFC 食物腐敗資料，否則可用放置／拆除重設食物新鮮度。 |

七個 heating recipe 全部為 200°C，參照 TFC 的熟食加熱溫度；輸入要求未腐敗，輸出使用 `tfc:copy_food` 保留時間資料。另產生 `tfc/item_heats/basic_ingredients`，為這些生熟材料提供 heat capacity 1.0。刪除六個空的手寫 heating JSON，並把 arthropod 的有效配方搬到生成資源，避免同 ID 重複。

既有切割配方保留 TFC 未腐敗檢查與食物資料複製；普通／發光生魷魚仍切出 TFC 魷魚肉，完整烤魷魚仍切出 MD 烤觸手。原版 `cutting/spider_leg` 與 `cutting/baked_spider_leg` 在該 JAR 中的 ID／輸入對調；停用後只保留目前 TFC provider 已正確區分的生、熟切割配方。

補上 MD 生觸手與烤觸手的固定 food data，分別參照 TFC 生／熟魷魚肉。新增加工食物使用 dynamic food data，不把完整配餐硬編成固定營養。

## 杯具與 Create 配方

原版四個 Create filling／emptying 配方全部停用，不產生仍使用舊 MD 杯具與 1000 mB 換算的替代配方。現在以先前實作的 MDTFC copper cup capability 盛裝 TFC 允許的液體；這份 patch 不新增 Create 依賴或 Create fluid-handler 相容性宣稱。

舊 `water_cup`、`milk_cup`、`powdered_snow_cup` 是原 MD 的分立物品，沒有新增配方；液體改由 MDTFC 同一個 cup 的內容物表達。既有湯杯仍沿用 cooking／cup conversion 機制，不恢復原版食物合成途徑。

## 同時提供的穿刺掉落補全

原版條件是手持 `farmersdelight:tools/knives`，不是 TFC damage type。第一份 patch 產生同 ID 的 **14 個 global loot modifier 覆寫**，把正向刀具條件及反向 knifeless 條件都改接現有 `minersdelighttfc:piercing_damage`。不另註冊第二套 modifier，因此不會因同時符合刀具與穿刺而重複執行。

| 生物／條件 | 原有掉落，現在改由穿刺判定 |
| --- | --- |
| 蝙蝠 | 穿刺必掉 2 蝙蝠翅；非穿刺仍為 50% 掉 1。 |
| 蜘蛛、洞穴蜘蛛 | 穿刺掉 1–3 arthropod。原有著火時的熟 arthropod modifier 也保留。 |
| 未著火的普通蜘蛛 | 40%＋每級 Looting 10% 觸發 0–4 生蜘蛛腿，保留原本數量範圍。 |
| 著火的蜘蛛、洞穴蜘蛛 | 同一觸發機率掉 1 烤蜘蛛腿。 |
| 未著火的蜘蛛、洞穴蜘蛛 | 穿刺時 20% 掉蜘蛛網。 |
| 蠹魚 | 穿刺必掉 1 arthropod，另有 50% 卵掉落；非穿刺卵機率仍為 5%。 |
| 原版魷魚、發光魷魚 | 穿刺分別掉完整 squid／glow_squid；非穿刺仍掉 1–3 tentacles。 |

TFC 魷魚與 `tfc:octopoteuthis` 繼續使用你已 push 的掉落表，不再額外加一套。

這個 backport 的幾個 modifier 寫成 `minecraft:any_of`；覆寫時一併改為 1.20.1 使用的 `minecraft:alternative`，否則相關分支無法正確反序列化。原始 JAR 的 global 清單還引用五個不存在的 modifier JSON，本次沒有憑缺檔名稱推測、創造額外掉落。

## 全部停用的上游配方與對應路徑

下表原始 ID 省略 `miners_delight:` 前綴，替代 ID 省略 `minersdelighttfc:` 前綴。全部原 ID 都輸出 false condition，而不是將替代配方放回原 namespace。

| 原始 Recipe ID | 替代／處理方式 |
| --- | --- |
| `alt_insect_wrap` | `crafting/alt_insect_wrap` |
| `baked_cave_carrot` | `heating/cave_carrot` |
| `baked_cave_carrot_campfire` | `heating/cave_carrot` |
| `baked_cave_carrot_smoking` | `heating/cave_carrot` |
| `baked_spider_leg` | `heating/spider_leg` |
| `baked_spider_leg_campfire` | `heating/spider_leg` |
| `baked_spider_leg_smoking` | `heating/spider_leg` |
| `baked_squid` | `heating/squid`、`heating/glow_squid_to_baked_squid` |
| `baked_squid_campfire` | `heating/squid`、`heating/glow_squid_to_baked_squid` |
| `baked_squid_smoking` | `heating/squid`、`heating/glow_squid_to_baked_squid` |
| `baked_tentacles` | `heating/tentacles` |
| `baked_tentacles_campfire` | `heating/tentacles` |
| `baked_tentacles_smoking` | `heating/tentacles` |
| `bat_cookie` | `crafting/bat_cookie` |
| `cave_carrot_crate` | `crafting/cave_carrot_crate` |
| `cave_carrots_from_crate` | 不恢復；箱子僅作裝飾，避免腐敗資料重設 |
| `cave_hamburger` | `crafting/cave_hamburger` |
| `cooked_arthropod` | `heating/arthropod` |
| `copper_carrot` | `cooking/copper_carrot` |
| `copper_cup` | `anvil/copper_cup`（MDTFC cup） |
| `copper_pot` | `anvil/copper_pot` |
| `create/emptying/empty_milk_cup` | 不恢復；使用 MDTFC cup 的流體 capability |
| `create/emptying/empty_water_cup` | 不恢復；使用 MDTFC cup 的流體 capability |
| `create/filling/fill_milk_cup` | 不恢復；使用 MDTFC cup 的流體 capability |
| `create/filling/fill_water_cup` | 不恢復；使用 MDTFC cup 的流體 capability |
| `crunchy_bar` | `crafting/crunchy_bar` |
| `cutting/baked_spider_leg` | `cutting/baked_spider_leg` |
| `cutting/baked_squid` | `cutting/baked_squid` |
| `cutting/bat_wing` | `cutting/bat_wing` |
| `cutting/glow_squid` | `cutting/glow_squid` |
| `cutting/moss` | `cutting/moss` |
| `cutting/spider_leg` | `cutting/spider_leg` |
| `cutting/squid` | `cutting/squid` |
| `cutting/vegan_patty` | `cutting/vegan_patty` |
| `cutting/wild_cave_carrot` | `cutting/wild_cave_carrot` |
| `glazed_arachnid_limbs` | `cooking/glazed_arachnid_limbs` |
| `golden_nutritional_bar` | `cooking/golden_nutritional_bar` |
| `improvised_barbecue_stick` | `crafting/improvised_barbecue_stick` |
| `insect_sandwich` | `crafting/insect_sandwich` |
| `insect_wrap` | `crafting/insect_wrap` |
| `nutritional_bar` | `cooking/nutritional_bar` |
| `seasoned_arthropods` | `cooking/seasoned_arthropods` |
| `smoked_bat_wing` | `heating/bat_wing` |
| `smoked_bat_wing_campfire` | `heating/bat_wing` |
| `smoked_bat_wing_smoking` | `heating/bat_wing` |
| `squid_sandwich` | `crafting/squid_sandwich` |
| `sticky_basket` | `crafting/sticky_basket` |
| `string_from_gossypium` | `crafting/string_from_gossypium` |
| `tentacles_on_a_stick` | `crafting/tentacles_on_a_stick` |
| `vegan_hamburger` | `crafting/vegan_hamburger` |
| `vegan_steak_and_potatoes` | `cooking/vegan_steak_and_potatoes` |
| `vegan_wrap` | `crafting/vegan_wrap` |
| `weird_caviar` | `crafting/weird_caviar` |

## 驗證結果

- 已實跑 `runData`：全部 provider 成功；只略過不影響資料生成的遊戲 asset／音效下載任務。
- 已通過完整 `build`，包含 Java 編譯及 Forge reobfuscation。
- 已檢查成品 JAR 內的生成資源、provider 類別與 Miner’s Delight 載入順序。
- 生成結果：53 個上游配方停用檔、14 個掉落 modifier 覆寫、50 個 MDTFC 配方、47 份 food data、1 份涵蓋 13 個生熟材料的 heat data。
- 所有 16 個 cooking pot 配方都不超過六個輸入格；12 種新增製作路徑的食物都有 dynamic food data。
- 展開配方實際使用的食材標籤後，共檢查 160 個可能的輸入物品。原版 ID 僅出現碗、蜘蛛網、發光墨囊、苔蘚方塊，以及允許的 sweetener 糖，沒有禁止的原版食物或金屬原料。
- 比較 14 份掉落 JSON 的完整結構，確認除了刀具分類替換及 `any_of` 版本修正外，原有數量、機率、著火與反向條件均保留。
- 尚未進入遊戲世界實測種植、擊殺、鍋具操作與食用效果；建置及 datagen 成功不等同這些互動已實測。
