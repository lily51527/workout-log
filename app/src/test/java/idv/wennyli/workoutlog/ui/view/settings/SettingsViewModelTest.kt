package idv.wennyli.workoutlog.ui.view.settings

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    @Test
    fun `init should load userProfile and map to UiState correctly`() = runTest {
        // Arrange (setup 已完成)

        // Act & Assert
        // 驗證 ViewModel 的 userProfile StateFlow 是否正確反映了 Repository 的資料
        val currentUserProfile = viewModel.userProfile.value

        assertThat(currentUserProfile).isNotNull()
        assertThat(currentUserProfile).isEqualTo(fakeUserProfile)
        assertThat(currentUserProfile?.gender).isEqualTo("male")
        assertThat(currentUserProfile?.birthDate).isEqualTo("1990-01-01")
    }

    @Test
    fun `updateUserProfile should call repository update with correct parameters`() = runTest {
        // Arrange
        // 模擬 Repository update 成功
        coEvery { mockUserProfileRepository.updateUserProfile(any()) } returns Unit

        // 假設我們要更新性別，但保留原本的生日
        val newGender = "female"

        // Act
        viewModel.updateUserProfile(gender = newGender)

        // Assert
        // 驗證 Repository 被呼叫，且資料是 "新性別" + "舊生日" (因為 setup 中已載入 fakeUserProfile)
        coVerify(exactly = 1) {
            mockUserProfileRepository.updateUserProfile(
                match {
                    it.gender == newGender &&
                            it.birthDate == fakeUserProfile.birthDate
                }
            )
        }
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

    // 邊界測試 - 當 UserProfile 尚未載入時的更新行為
    @Test
    fun `updateUserProfile should create new profile if current profile is null`() = runTest {
        // Arrange
        // 1. 模擬 UserProfileRepository 回傳 null (代表尚未載入或沒有資料)
        every { mockUserProfileRepository.getUserProfile() } returns flowOf(null)

        // 2. 重新初始化 ViewModel，使其 userProfile 為 null
        val nullSettingViewModel = SettingsViewModel(
            userProfileRepository = mockUserProfileRepository,
            bodyMeasurementRepository = mockBodyMeasurementRepository
        )

        // 3. 模擬 Repository update 成功
        coEvery { mockUserProfileRepository.updateUserProfile(any()) } returns Unit

        val newGender = "female"

        // Act
        nullSettingViewModel.updateUserProfile(gender = newGender)

        // Assert
        // 驗證 Repository 被呼叫，且資料是基於 "空 Profile" 加上 "新性別"
        // 預期 birthDate 應該是預設值空字串 ""
        coVerify(exactly = 1) {
            mockUserProfileRepository.updateUserProfile(
                match {
                    it.gender == newGender && it.birthDate == ""
                }
            )
        }
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

    @Test
    fun `init should handle error when load measurements fails`() = runTest {
        // Arrange - 特殊情況，需要在 init 前重新設定 mock
        val errorMsg = "Load Failed"
        // 我們需要重新建立一個 ViewModel 來觸發 init，並讓它讀取到會拋出錯誤的 Flow
        every { mockBodyMeasurementRepository.getBodyMeasurements() } returns flow {
            throw Exception(
                errorMsg
            )
        }

        // Act - 重新初始化 ViewModel
        val errorViewModel = SettingsViewModel(
            userProfileRepository = mockUserProfileRepository,
            bodyMeasurementRepository = mockBodyMeasurementRepository
        )

        // Assert
        // 這裡需要用 turbine 或是直接檢查 value，因為我們用了 UnconfinedTestDispatcher
        assertThat(errorViewModel.error.value).contains("載入數據失敗")
        assertThat(errorViewModel.error.value).contains(errorMsg)
    }

    @Test
    fun `updateUserProfile should handle repository exception`() = runTest {
        // Arrange
        val errorMsg = "Network Error"
        coEvery { mockUserProfileRepository.updateUserProfile(any()) } throws Exception(errorMsg)

        // Act
        viewModel.updateUserProfile(birthDate = "2000-01-01")

        // Assert
        assertThat(viewModel.error.value).contains("更新個人資料失敗")
        assertThat(viewModel.error.value).contains(errorMsg)
    }

    @Test
    fun `init should handle error when load UserProfile fails`() = runTest {
        val errorMsg = "Load Failed"
        every { mockUserProfileRepository.getUserProfile() } returns flow {
            throw Exception(
                errorMsg
            )
        }

        val errorViewModel = SettingsViewModel(
            userProfileRepository = mockUserProfileRepository,
            bodyMeasurementRepository = mockBodyMeasurementRepository
        )

        assertThat(errorViewModel.error.value).contains("載入個人資料失敗")
        assertThat(errorViewModel.error.value).contains(errorMsg)
    }

    // --- CORRECT 補強測試 ---

    // [C]onformance: 格式不符 (輸入非數字)
    @Test
    fun `addBodyMeasurement should fail when input is not a number`() = runTest {
        // Arrange
        val invalidHeight = "abc" // 非數字
        val validWeight = "70"
        val validFat = "20"

        // Act
        viewModel.addBodyMeasurement(invalidHeight, validWeight, validFat)

        // Assert
        coVerify(exactly = 0) { mockBodyMeasurementRepository.addBodyMeasurement(any()) }
        assertThat(viewModel.error.value).isEqualTo("請輸入有效的身高和體重")
    }

    // [E]xistence: 存在性 (輸入空字串)
    @Test
    fun `addBodyMeasurement should fail when input is empty`() = runTest {
        // Arrange
        val emptyHeight = "" // 空字串
        val validWeight = "70"
        val validFat = "20"

        // Act
        viewModel.addBodyMeasurement(emptyHeight, validWeight, validFat)

        // Assert
        coVerify(exactly = 0) { mockBodyMeasurementRepository.addBodyMeasurement(any()) }
        assertThat(viewModel.error.value).isEqualTo("請輸入有效的身高和體重")
    }

    // [R]ange: 邊界範圍 (剛好是 0)
    @Test
    fun `addBodyMeasurement should fail when weight is zero`() = runTest {
        // Arrange
        val validHeight = "180"
        val zeroWeight = "0" // 邊界值
        val validFat = "20"

        // Act
        viewModel.addBodyMeasurement(validHeight, zeroWeight, validFat)

        // Assert
        coVerify(exactly = 0) { mockBodyMeasurementRepository.addBodyMeasurement(any()) }
        assertThat(viewModel.error.value).isEqualTo("請輸入有效的身高和體重")
    }

    // [T]ime: 時間格式驗證 (針對 private fun toUiState 的間接測試)
    @Test
    fun `measurementUiStateList should format date correctly`() = runTest {
        // Arrange
        // 固定一個時間點，例如 2026-02-23
        val fakeDateString = "2026-02-23"
        val fixDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fakeDateString)
        val measurement =
            BodyMeasurement(id = "", height = 180.0, weight = 70.0, timestamp = fixDate)

        every { mockBodyMeasurementRepository.getBodyMeasurements() } returns flowOf(
            listOf(
                measurement
            )
        )

        // Act
        // 重新 init 讓它讀取這個固定的時間
        val viewModel = SettingsViewModel(
            userProfileRepository = mockUserProfileRepository,
            bodyMeasurementRepository = mockBodyMeasurementRepository
        )

        // Assert
        // 驗證我們 private fun toUiState 的邏輯
        val uiState = viewModel.measurementUiStateList.value.first()
        assertThat(uiState.formattedDate).isEqualTo(fakeDateString)
    }
}