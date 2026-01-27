package idv.wennyli.workoutlog

import com.google.common.truth.Truth.assertThat
import idv.wennyli.workoutlog.data.model.BodyMeasurement
import idv.wennyli.workoutlog.data.model.UserProfile
import idv.wennyli.workoutlog.data.repository.BodyMeasurementRepository
import idv.wennyli.workoutlog.data.repository.UserProfileRepository
import idv.wennyli.workoutlog.ui.view.settings.SettingsViewModel
import idv.wennyli.workoutlog.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    // 1. 必備：處理 viewModelScope 的 Main Thread 問題
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Mock 依賴
    private lateinit var mockUserProfileRepository: UserProfileRepository
    private lateinit var mockBodyMeasurementRepository: BodyMeasurementRepository

    private lateinit var viewModel: SettingsViewModel

    // 假資料
    private val fakeUserProfile = UserProfile(gender = "male", birthDate = "1990-01-01")

    private val fakeBodyMeasurements = listOf(
        BodyMeasurement(
            id = "1",
            height = 170.0,
            weight = 70.0,
            bodyFat = 15.0,
            timestamp = Date(200L)
        ),
        BodyMeasurement(
            id = "2",
            height = 160.0,
            weight = 60.0,
            bodyFat = 20.0,
            timestamp = Date(100L)
        )
    )

    @Before
    fun setup() {
        mockUserProfileRepository = mockk()
        mockBodyMeasurementRepository = mockk()

        // 設定 Repository 的預設行為 (Happy Path)
        // 注意：因為 ViewModel 的 init 區塊會馬上呼叫這些方法，所以必須在建立 ViewModel 之前設定好 Mock
        every { mockUserProfileRepository.getUserProfile() } returns flowOf(fakeUserProfile)
        every { mockBodyMeasurementRepository.getBodyMeasurements() } returns flowOf(
            fakeBodyMeasurements
        )

        // 初始化 ViewModel
        viewModel = SettingsViewModel(
            userProfileRepository = mockUserProfileRepository,
            bodyMeasurementRepository = mockBodyMeasurementRepository
        )
    }

    // --- Right (正確路徑測試) ---

    @Test
    fun `init should load measurements and map to UiState correctly`() = runTest {
        // Arrange (在 setup 完成)

        // Act & Assert
        // 驗證 ViewModel 的 State 是否正確反映了 Repository 的資料
        val currentUiState = viewModel.measurementUiStateList.value
        assertThat(currentUiState).hasSize(2)
        // 驗證第一筆資料的細節字串是否符合預期格式
        assertThat(currentUiState[0].details).contains("身高: 170.0 cm")
        assertThat(currentUiState[0].details).contains("體重: 70.0 kg")
        assertThat(currentUiState[0].details).contains("體脂: 15.0%")
    }

    @Test
    fun `addBodyMeasurement should call repository add with correct parameters`() = runTest {
        // Arrange
        val heightStr = "175.5"
        val weightStr = "72.0"
        val bodyFatStr = "20.5"

        // 模擬 Repository add 成功
        coEvery { mockBodyMeasurementRepository.addBodyMeasurement(any()) } returns Unit

        // Act
        viewModel.addBodyMeasurement(heightStr, weightStr, bodyFatStr)

        // Assert
        // 驗證 Repository 的 add 方法被呼叫，且參數正確轉換為 Double
        coVerify(exactly = 1) {
            mockBodyMeasurementRepository.addBodyMeasurement(
                match {
                    it.bodyFat == 20.5 &&
                            it.height == 175.5 &&
                            it.weight == 72.0
                }
            )
        }

        // 驗證錯誤訊息應該要是空的
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `deleteBodyMeasurement should call repository delete`() = runTest {
        // Arrange
        val idToDelete = "123"
        coEvery { mockBodyMeasurementRepository.deleteBodyMeasurement(any()) } returns Unit

        // Act
        viewModel.deleteBodyMeasurement(idToDelete)

        // Assert
        coVerify(exactly = 1) { mockBodyMeasurementRepository.deleteBodyMeasurement(idToDelete) }
    }

    // --- Boundary / Logic (邊界與邏輯測試) ---

    @Test
    fun `addBodyMeasurement should show error and NOT call repository if inputs are invalid`() =
        runTest {
            // Arrange
            val invalidHeight = "-100"
            val validWeight = "70"
            val validBodyFat = "20"

            // Act
            viewModel.addBodyMeasurement(invalidHeight, validWeight, validBodyFat)

            // Assert
            // 1. 驗證 Repository 根本沒有被呼叫
            coVerify(exactly = 0) { mockBodyMeasurementRepository.addBodyMeasurement(any()) }

            // 2. 驗證 Error State 有值 (對應程式碼中的 "請輸入有效的身高和體重")
            assertThat(viewModel.error.value).isEqualTo("請輸入有效的身高和體重")
        }

    @Test
    fun `addBodyMeasurement should show error if body fat is negative`() = runTest {
        // Arrange
        val validHeight = "180"
        val validWeight = "70"
        val invalidBodyFat = "-20"

        // Act
        viewModel.addBodyMeasurement(validHeight, validWeight, invalidBodyFat)

        // Assert
        coVerify(exactly = 0) { mockBodyMeasurementRepository.addBodyMeasurement(any()) }
        assertThat(viewModel.error.value).isEqualTo("體脂肪率不能為負數。")
    }

    // --- Error (錯誤處理測試) ---

    @Test
    fun `addBodyMeasurement should update error state when repository throws exception`() =
        runTest {
            // Arrange
            val errorMsg = "Database Error"
            // 模擬 Repository 拋出異常
            coEvery { mockBodyMeasurementRepository.addBodyMeasurement(any()) } throws Exception(
                errorMsg
            )

            // Act
            viewModel.addBodyMeasurement("180", "70", "20")

            // Assert
            // 驗證 ViewModel 捕獲了異常並更新了 error state
            assertThat(viewModel.error.value).contains("新增失敗")
            assertThat(viewModel.error.value).contains(errorMsg)
        }

}