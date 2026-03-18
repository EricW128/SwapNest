package com.zixuan_wang.swapnest.repository

import android.net.Uri
import com.zixuan_wang.swapnest.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    suspend fun addItem(item: Item, imageUri: Uri?): Item
    suspend fun getItemById(id: String): Item?
}

class MockItemRepository : ItemRepository {
    private val mockItems = mutableListOf(
        Item(id = "1", title = "复古台灯", description = "成色极好，暖色调光，适合阅读。", category = "居家", condition = "九成新", imageUrl = "https://picsum.photos/seed/1/400/300", ownerName = "张三", type = "Donation", latitude = 31.2304, longitude = 121.4737),
        Item(id = "2", title = "山地自行车", description = "捷安特经典款，变速灵敏，轮胎刚换。", category = "运动", condition = "八成新", imageUrl = "https://picsum.photos/seed/2/400/300", ownerName = "李四", type = "Exchange", latitude = 31.2354, longitude = 121.4787),
        Item(id = "3", title = "意式咖啡机", description = "全自动操作，奶泡细腻，适合新手。", category = "家电", condition = "全新", imageUrl = "https://picsum.photos/seed/3/400/300", ownerName = "王五", type = "Donation", latitude = 31.2204, longitude = 121.4637),
        Item(id = "4", title = "桌游合集", description = "包含卡坦岛、卡卡颂，周末聚会神器。", category = "玩具", condition = "九成新", imageUrl = "https://picsum.photos/seed/4/400/300", ownerName = "赵六", type = "Exchange", latitude = 31.2404, longitude = 121.4837),
        Item(id = "5", title = "iPad Pro 2021", description = "M1芯片，11寸，带原装笔。", category = "电子产品", condition = "九五新", imageUrl = "https://picsum.photos/seed/5/400/300", ownerName = "钱七", type = "Exchange", latitude = 31.2104, longitude = 121.4537),
        Item(id = "6", title = "《小王子》精装本", description = "经典文学，纸张考究，值得收藏。", category = "图书文具", condition = "全新", imageUrl = "https://picsum.photos/seed/6/400/300", ownerName = "孙八", type = "Donation", latitude = 31.2504, longitude = 121.4937),
        Item(id = "7", title = "SK-II 神仙水", description = "230ml，还有大半瓶，肤质不合转手。", category = "美妆个护", condition = "八成新", imageUrl = "https://picsum.photos/seed/7/400/300", ownerName = "周九", type = "Exchange", latitude = 31.2004, longitude = 121.4437),
        Item(id = "8", title = "瑜伽垫", description = "加厚防滑材质，基本没用过。", category = "运动户外", condition = "九九新", imageUrl = "https://picsum.photos/seed/8/400/300", ownerName = "吴十", type = "Donation", latitude = 31.2604, longitude = 121.5037),
        Item(id = "9", title = "空气炸锅", description = "4.5L大容量，健康无油烹饪。", category = "生活用品", condition = "九成新", imageUrl = "https://picsum.photos/seed/9/400/300", ownerName = "陈十一", type = "Donation", latitude = 31.2304, longitude = 121.4737),
        Item(id = "10", title = "机械键盘", description = "青轴手感，RGB背光，打字超爽。", category = "电子产品", condition = "八成新", imageUrl = "https://picsum.photos/seed/10/400/300", ownerName = "林十二", type = "Exchange", latitude = 31.2354, longitude = 121.4787),
        Item(id = "11", title = "拍立得 Mini11", description = "粉色少女心，含两盒相纸。", category = "电子产品", condition = "九成新", imageUrl = "https://picsum.photos/seed/11/400/300", ownerName = "徐十三", type = "Exchange", latitude = 31.2204, longitude = 121.4637),
        Item(id = "12", title = "手冲咖啡壶", description = "细口设计，控流精准。", category = "生活用品", condition = "全新", imageUrl = "https://picsum.photos/seed/12/400/300", ownerName = "高十四", type = "Donation", latitude = 31.2404, longitude = 121.4837),
        Item(id = "13", title = "哑铃套装", description = "2kg*2，适合居家健身。", category = "运动户外", condition = "九成新", imageUrl = "https://picsum.photos/seed/13/400/300", ownerName = "马十五", type = "Donation", latitude = 31.2104, longitude = 121.4537),
        Item(id = "14", title = "蓝牙音箱", description = "JBL音质，防水设计。", category = "电子产品", condition = "八成新", imageUrl = "https://picsum.photos/seed/14/400/300", ownerName = "朱十六", type = "Exchange", latitude = 31.2504, longitude = 121.4937),
        Item(id = "15", title = "乐高花束", description = "已拼好，带玻璃花瓶。", category = "图书文具", condition = "九九新", imageUrl = "https://picsum.photos/seed/15/400/300", ownerName = "胡十七", type = "Donation", latitude = 31.2004, longitude = 121.4437),
        Item(id = "16", title = "香薰机", description = "超声波雾化，助眠神器。", category = "美妆个护", condition = "九成新", imageUrl = "https://picsum.photos/seed/16/400/300", ownerName = "郭十八", type = "Exchange", latitude = 31.2604, longitude = 121.5037),
        Item(id = "17", title = "电热毯", description = "双人控温，冬天必备。", category = "生活用品", condition = "全新", imageUrl = "https://picsum.photos/seed/17/400/300", ownerName = "何十九", type = "Donation", latitude = 31.2304, longitude = 121.4737),
        Item(id = "18", title = "Kindle Paperwhite", description = "电纸书，护眼墨水屏。", category = "电子产品", condition = "七成新", imageUrl = "https://picsum.photos/seed/18/400/300", ownerName = "罗二十", type = "Exchange", latitude = 31.2354, longitude = 121.4787),
        Item(id = "19", title = "帆布袋", description = "原创插画设计，大容量。", category = "生活用品", condition = "全新", imageUrl = "https://picsum.photos/seed/19/400/300", ownerName = "宋二一", type = "Donation", latitude = 31.2204, longitude = 121.4637),
        Item(id = "20", title = "自动伞", description = "防晒防紫外线，轻便好拿。", category = "生活用品", condition = "九五新", imageUrl = "https://picsum.photos/seed/20/400/300", ownerName = "谢二二", type = "Donation", latitude = 31.2404, longitude = 121.4837)
    )

    override fun getItems(): Flow<List<Item>> = flowOf(mockItems)

    override suspend fun addItem(item: Item, imageUri: Uri?): Item {
        val newItem = item.copy(
            id = (mockItems.size + 1).toString(),
            imageUrl = item.imageUrl.ifBlank { "https://picsum.photos/seed/${System.currentTimeMillis()}/400/600" }
        )
        mockItems.add(newItem)
        return newItem
    }

    override suspend fun getItemById(id: String): Item? {
        return mockItems.find { it.id == id }
    }
}
