package com.xz.scorep.executor.utils;

import java.util.Random;

public class ChineseName {

    public static final String[] FAMILY_NAMES = new String[]{
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "卫",
            "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许", "何", "吕", "施", "张",
            "孔", "曹", "严", "华", "金", "魏", "陶", "姜", "戚", "谢", "邹",
            "柏", "窦", "章", "苏", "潘", "葛", "奚", "范", "彭",
            "鲁", "韦", "马", "苗", "花", "方", "俞", "任", "袁",
            "鲍", "史", "唐", "薛", "雷", "贺", "汤",
            "滕", "殷", "罗", "毕", "郝", "傅",
            "齐", "康", "伍", "余", "顾", "孟", "黄",
            "萧", "尹", "姚", "邵", "汪", "祁", "毛", "狄",
            "贝", "伏", "成", "戴", "宋", "茅", "庞",
            "纪", "舒", "屈", "项", "祝", "董", "梁", "杜", "阮", "蓝",
            "季", "贾", "娄", "江", "童", "颜", "郭",
            "梅", "林", "钟", "徐", "邱", "骆", "高", "夏", "蔡", "田",
            "樊", "胡", "凌", "霍", "虞", "万", "柯", "卢", "莫",
            "裘", "丁", "邓",
            "洪", "包", "左", "石", "崔", "龚",
            "程", "邢", "陆", "翁", "于", "惠",
            "曲", "羿", "靳", "松",
            "段", "焦", "谷",
            "侯",
            "宁", "戎", "刘",
            "景", "詹", "叶", "黎", "溥",
            "瞿", "阎", "连", "习", "艾", "容",
            "古", "易", "廖", "衡",
            "耿", "欧",
            "蔚", "越", "巩", "聂",
            "敖",
            "游",
            "岳", "帅", "况",
            "楚", "涂", "商",
            "阳",
            "楼", "高",
            "覃",
            "司马", "上官", "欧阳", "夏侯", "诸葛",
            "公孙", "令狐", "长孙", "慕容",
            "司徒"
    };

    public static final String CHARS = "大地为子中生国年着出得里后自以会家可下过天去能对小多然于心学么之都好看" +
            "发当成只如事用第样道种开美情己面女前所同日行意动方期经长回位爱给名法间知世" +
            "身者高亲其进常活正感见明问力理文定本公特做外相西果月十实向声车全信重三机工物气别真太新" +
            "比才夫再书部水等体却加主界门利更东记应直字场平报友关放至认入笑内英军候民岁往何度山路带万男" +
            "风解任金快原通师立象数四失满战远格士音目条始达深完今清王化空业思切钱语元喜离飞科言干流欢" +
            "约即合反题交林医晚制球决画保读运及房早院量火品产星精五连司奇管类未朋且婚台夜青北队久乎越" +
            "观落形影红百令识步希亚术留市半热送兴造谈容极随演收首根整式照办强石古华装双尼转诉米丽南领节衣" +
            "站黑刻统断福城故历惊脸选包争另建维树系示愿持千史准联基志静诗独复消算义竟酒单治幸兰念举钟" +
            "共息功官待究跟易游程号居考突皮费图刚永歌响商礼细专黄灵改据般破引存众注笔甚某沉备习默务土" +
            "微怀料调广苏赛查密议富梦座参八亮印线温虽掉京初养香际致阳李严证帝饭趣支春集丈木研班" +
            "普导顿展获艺六波察群皇段庭谢草排背止组州朝封睛板角况曲馆育质河续哥若推境遇雨标姐充围案伦护冷" +
            "贝著雪索剧烟依斗值帮汉闻沙局伯族低资屋击顾洲团圣堂兵七露园旅劳型烈鱼抱宝权鲁简票" +
            "寻律胜份汽右洋范舞午登楼贵责例追职属渐左录丝牙党继托赶章智冲叶胡吉卖坚救修松临藏担戏善卫药村戴" +
            "森耳祖云规窗散迷乡恩投弹铁博雷府超杂醒采毫毕九冰既状景席珍童顶派素农练按拍征余承置" +
            "彩灯巨琴环换技翻束增忍洛塞忆判层付岛项懂武革良恋委拥娜妙探营宣银势奖宫套康供优" +
            "降夏困健模败守鲜财孤杰迹妹遍盖副坦江顺秋菜划归浪凡预雄升典含盛济蒙棋端释介误";

    public static final Random RANDOM = new Random(System.currentTimeMillis());

    public static String nextRandomName() {

        String name = FAMILY_NAMES[RANDOM.nextInt(FAMILY_NAMES.length)];

        /* 从常用字中选取一个或两个字作为名 */
        if (RANDOM.nextBoolean()) {
            name += nextRandomChinese() + nextRandomChinese();
        } else {
            name += nextRandomChinese();
        }

        return name;
    }

    private static String nextRandomChinese() {
        return new String(new char[]{CHARS.charAt(RANDOM.nextInt(CHARS.length()))});
    }

}