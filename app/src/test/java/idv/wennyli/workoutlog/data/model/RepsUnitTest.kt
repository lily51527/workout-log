package idv.wennyli.workoutlog.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RepsUnitTest {

    // 傳入合法的 displayName "次數"，應回傳 COUNT
    @Test
    fun `fromDisplayName should return COUNT when name is 次數`() {
        assertThat(RepsUnit.fromDisplayName("次數")).isEqualTo(RepsUnit.COUNT)
    }

    // 傳入合法的 displayName "秒數"，應回傳 SECONDS
    @Test
    fun `fromDisplayName should return SECONDS when name is 秒數`() {
        assertThat(RepsUnit.fromDisplayName("秒數")).isEqualTo(RepsUnit.SECONDS)
    }

    // 傳入 null（Firestore 欄位不存在時），應 fallback 回傳 COUNT
    @Test
    fun `fromDisplayName should return COUNT when name is null`() {
        assertThat(RepsUnit.fromDisplayName(null)).isEqualTo(RepsUnit.COUNT)
    }

    // 傳入未知字串（資料損壞或新增 enum 前的舊資料），應 fallback 回傳 COUNT
    @Test
    fun `fromDisplayName should return COUNT when name is unknown`() {
        assertThat(RepsUnit.fromDisplayName("unknown")).isEqualTo(RepsUnit.COUNT)
    }
}
