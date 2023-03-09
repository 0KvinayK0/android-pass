package proton.android.pass.featurevault.impl.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCreateVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

class CreateVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: CreateVaultViewModel
    private lateinit var snackbar: TestSnackbarMessageRepository
    private lateinit var createVault: TestCreateVault

    @Before
    fun setup() {
        snackbar = TestSnackbarMessageRepository()
        createVault = TestCreateVault()
        instance = CreateVaultViewModel(
            snackbar,
            createVault,
            TestEncryptionContextProvider()
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(CreateVaultUiState.Initial)
        }
    }

    @Test
    fun `holds name changes`() = runTest {
        val name = "test"
        instance.onNameChange(name)
        instance.state.test {
            assertThat(awaitItem().name).isEqualTo(name)
        }
    }

    @Test
    fun `holds icon changes`() = runTest {
        val icon = ShareIcon.Icon8
        instance.onIconChange(icon)
        instance.state.test {
            assertThat(awaitItem().icon).isEqualTo(icon)
        }
    }

    @Test
    fun `holds color changes`() = runTest {
        val color = ShareColor.Color7
        instance.onColorChange(color)
        instance.state.test {
            assertThat(awaitItem().color).isEqualTo(color)
        }
    }

    @Test
    fun `entering and clearing the text disables button and shows error`() = runTest {
        instance.onNameChange("name")
        instance.onNameChange("")
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isCreateButtonEnabled).isEqualTo(IsButtonEnabled.Disabled)
            assertThat(item.isTitleRequiredError).isTrue()
        }
    }

    @Test
    fun `displays error snackbar on createVault error`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(LoadingResult.Error(IllegalStateException("test")))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Unknown)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultError)
        }
    }

    @Test
    fun `displays success snackbar on createVault success`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(LoadingResult.Success(TestShare.create()))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Created)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultSuccess)
        }
    }
}