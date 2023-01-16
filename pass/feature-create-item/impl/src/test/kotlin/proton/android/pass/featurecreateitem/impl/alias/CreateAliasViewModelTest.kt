package proton.android.pass.featurecreateitem.impl.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.pass.domain.AliasOptions
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.data.TestAliasRepository
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var suffix: AliasSuffixUiModel
    private lateinit var mailbox: AliasMailboxUiModel
    private lateinit var viewModel: CreateAliasViewModel
    private lateinit var aliasRepository: TestAliasRepository
    private lateinit var createAlias: TestCreateAlias
    private lateinit var snackbarRepository: TestSnackbarMessageRepository

    @Before
    fun setUp() {
        suffix = TestAliasSuffixUiModel.create()
        mailbox = TestAliasMailboxUiModel.create()

        aliasRepository = TestAliasRepository()
        aliasRepository.setAliasOptions(
            AliasOptions(
                suffixes = listOf(suffix.toDomain()),
                mailboxes = listOf(mailbox.toDomain())
            )
        )

        createAlias = TestCreateAlias()
        snackbarRepository = TestSnackbarMessageRepository()
    }


    @Test
    fun `title alias sync`() = runTest {
        viewModel = createAliasViewModel()

        val titleInput = "Title changed"
        viewModel.onSuffixChange(suffix)
        viewModel.onTitleChange(titleInput)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.aliasItem.alias).isEqualTo("title-changed")
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("title-changed${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newAlias = "myalias"
        viewModel.onAliasChange(newAlias)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.aliasItem.alias).isEqualTo(newAlias)
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newTitle = "New title"
        viewModel.onTitleChange(newTitle)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(newTitle)
            assertThat(item.aliasItem.alias).isEqualTo(newAlias)
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no suffix when the alias has changed the state should hold it`() = runTest {
        viewModel = createAliasViewModel()

        val aliasInput = "aliasInput"
        viewModel.onAliasChange(aliasInput)

        viewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(CreateUpdateAliasUiState.Initial.aliasItem.copy(alias = aliasInput))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `is able to handle CannotCreateMoreAliases`() = runTest {
        viewModel = createAliasViewModel()

        createAlias.setResult(Result.Error(CannotCreateMoreAliasesError()))
        setupContentsForCreation()

        viewModel.createAlias(TestShare.create().id)
        snackbarRepository.snackbarMessage.test {
            val message = awaitItem()
            assertThat(message.isNotEmpty()).isTrue()
            message.map {
                assertThat(it).isEqualTo(AliasSnackbarMessage.CannotCreateMoreAliasesError)
            }
        }
    }

    @Test
    fun `emits success when alias is created successfully`() = runTest {
        viewModel = createAliasViewModel()

        createAlias.setResult(Result.Success(TestItem.random()))
        setupContentsForCreation()

        viewModel.createAlias(TestShare.create().id)
        viewModel.aliasUiState.test {
            skipItems(2)
            val item = awaitItem()

            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.isAliasSavedState).isInstanceOf(AliasSavedState.Success::class.java)
        }
    }

    @Test
    fun `spaces in title are properly formatted`() = runTest {
        viewModel = createAliasViewModel()

        val titleInput = "ThiS iS a TeSt"
        viewModel.onTitleChange(titleInput)
        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.alias).isEqualTo("this-is-a-test")
        }
    }

    @Test
    fun `setInitialState properly formats alias`() = runTest {
        viewModel = createAliasViewModel(title = "ThiS.iS_a TeSt")
        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.alias).isEqualTo("this.is_a-test")
        }
    }

    private fun createAliasViewModel(title: String? = null, isDraft: Boolean = false) =
        CreateAliasViewModel(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("123"))
            },
            aliasRepository = aliasRepository,
            createAlias = createAlias,
            snackbarMessageRepository = snackbarRepository,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set("shareId", "123")
                set("isDraft", isDraft)
                title?.let {
                    set("aliasTitle", title)
                }
            }
        )

    private fun setupContentsForCreation() {
        viewModel.aliasItemState.update {
            AliasItem(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(model = mailbox, selected = false)
                )
            )
        }

        viewModel.onTitleChange("title")
        viewModel.onAliasChange("alias")
        viewModel.onSuffixChange(suffix)
        viewModel.onMailboxesChanged(
            listOf(
                SelectedAliasMailboxUiModel(
                    model = mailbox,
                    selected = true
                )
            )
        )
    }
}