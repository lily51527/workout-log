package idv.wennyli.workoutlog.data.repository

import javax.inject.Inject

// Repository 介面，定義訓練動作相關資料的合約
interface ExerciseRepository {
    fun getExerciseToMuscleMap(): Map<String, String>
}

// Repository 的實作，目前以靜態資料提供訓練動作與肌群對應表
class ExerciseRepositoryImpl @Inject constructor() : ExerciseRepository {

    override fun getExerciseToMuscleMap(): Map<String, String> = mapOf(
        "臥推" to "胸大肌 (中束), 三角肌 (前束), 三頭肌",
        "划船" to "闊背肌, 斜方肌, 菱形肌, 二頭肌",
        "深蹲" to "股四頭肌, 臀大肌, 股二頭肌",
        "硬舉" to "臀大肌, 股二頭肌, 豎脊肌, 闊背肌",
        "肩推" to "三角肌 (前束, 中束), 三頭肌",
        "二頭彎舉" to "二頭肌",
        "三頭下壓" to "三頭肌",
        "引體向上" to "闊背肌, 二頭肌, 斜方肌",
        "腿推" to "股四頭肌, 臀大肌, 股二頭肌",
        "腿彎舉" to "股二頭肌",
        "腿伸展" to "股四頭肌",
        "側平舉" to "三角肌 (中束)",
        "飛鳥" to "胸大肌 (中束), 三角肌 (前束)",
        "羅馬尼亞硬舉" to "股二頭肌, 臀大肌, 豎脊肌",
        "農夫走路" to "核心肌群, 前臂肌群, 股四頭肌, 臀大肌",
        "平板支撐" to "腹直肌, 腹斜肌, 豎脊肌",
        "捲腹" to "腹直肌",
        "小腿提踵" to "腓腸肌, 比目魚肌",
        "上斜胸推" to "胸大肌 (上束), 三角肌 (前束), 三頭肌",
        "水平胸推" to "胸大肌 (中束), 三角肌 (前束), 三頭肌",
        "下斜胸推" to "胸大肌 (下束), 三角肌 (前束), 三頭肌",
        "蝴蝶機(夾胸)" to "胸大肌 (中束)",
        "反向飛鳥" to "三角肌 (後束), 斜方肌, 菱形肌",
        "保加利雅分腿蹲" to "股四頭肌, 臀大肌, 股二頭肌",
        "仰臥踢腿" to "腹直肌 (下腹), 髂腰肌",
        "屈膝捲腹" to "腹直肌",
        "滑輪下拉" to "闊背肌, 二頭肌",
        "棒式" to "腹直肌, 腹斜肌, 豎脊肌",
        "側棒式" to "腹斜肌, 臀中肌",
        "哈克深蹲" to "股四頭肌, 臀大肌",
        "爬梯機" to "股四頭肌, 臀大肌, 股二頭肌, 小腿肌群",
        "45度腿推" to "股四頭肌, 臀大肌, 股二頭肌"
    )
}
