package app.springdata.therapist.view.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import app.springdata.therapist.BaseFragment
import app.springdata.therapist.R
import app.springdata.therapist.databinding.FragmentItemsBinding
import app.springdata.therapist.model.KidObjectiveItem

@AndroidEntryPoint
class ItemsFragment : BaseFragment() {

    private lateinit var binding: FragmentItemsBinding
    private val viewModel: ItemsViewModel by viewModels()
    private lateinit var itemsAdapter: ItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentItemsBinding.inflate(inflater, container, false)
        observeData(viewModel)
        viewModel.takeAction(ItemAction.Start)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureViews()
    }

    override fun onResume() {
        super.onResume()
        // Set soft input mode to adjustPan for this fragment
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onPause() {
        super.onPause()
        // Restore original soft input mode (adjustResize)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun observeData(viewModel: ItemsViewModel) {
        val stateObserver = Observer<ItemState?> {
            // null state indicates there is no action needed
            it ?: return@Observer

            // Hide the loading state
            if (it != ItemState.Loading) {
                hideLoading()
            }

            when (it) {
                is ItemState.Loading -> showLoading()
                is ItemState.Error -> showError(it.generalError)
                is ItemState.Idle -> {}
                is ItemState.ContentLoaded -> handleContentLoaded(it.items)
                is ItemState.GoToObjectives -> navigateToObjectives()
            }
        }
        viewModel.state.observe(viewLifecycleOwner, stateObserver)

        viewModel.title.observe(viewLifecycleOwner) { title ->
            binding.subtitleInclude.subtitleTextView.isVisible = true
            binding.subtitleInclude.subtitleTextView.text = title
        }

        viewModel.coordinatorName.observe(viewLifecycleOwner) {
            configureEmptyState(it)
        }
    }

    private fun configureViews() {
        hideBottomBar()
        configureEmptyState()

        itemsAdapter = ItemsAdapter(
            onYesClicked = { item ->
                viewModel.takeAction(ItemAction.ItemYesClicked(item))
            },
            onNoClicked = { item ->
                viewModel.takeAction(ItemAction.ItemNoClicked(item))
            },
            onToggleClicked = { item ->
                viewModel.takeAction(ItemAction.ItemToggleClicked(item))
            },
            onFrequencySet = { item, freq, interval ->
                viewModel.takeAction(ItemAction.FrequencySet(item, freq, interval))
            },
            onProgressSet = { item, progress ->
                viewModel.takeAction(ItemAction.ProgressSet(item, progress))
            },
            onCheckmarkClicked = { item ->
                viewModel.takeAction(ItemAction.CheckmarkClicked(item))
            }
        )

        binding.itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemsAdapter
        }
    }

    private fun handleContentLoaded(items: List<KidObjectiveItem>) {
        itemsAdapter.updateData(items)
        binding.itemsRecyclerView.isVisible = items.isNotEmpty()
        binding.emptyStateInclude.emptyStateView.isVisible = items.isEmpty()
    }

    private fun configureEmptyState(coordinatorName: String? = null) {
        val coordinatorLabel = coordinatorName?.takeIf { it.isNotBlank() }?.let { "Coordinator $it" }
            ?: "your Coordinator"
        binding.emptyStateInclude.emptyTitleTextView.text = "No items here \uD83D\uDE42"
        binding.emptyStateInclude.emptyMessageTextView.text =
            "You completed these items for today. Un-mastered items will appear here tomorrow or when you have your next session 👍. $coordinatorLabel will add more items if needed."
    }

    private fun showError(generalError: String) {
        binding.subtitleInclude.errorTextView.text = generalError
        binding.subtitleInclude.errorTextView.visibility = View.VISIBLE
        binding.emptyStateInclude.emptyStateView.isVisible = false
    }

    private fun navigateToObjectives() {
        findNavController().navigate(R.id.objectivesFragment)
    }

    private fun showLoading() = binding.loadingViewInclude.loadingView.apply { visibility = View.VISIBLE }

    private fun hideLoading() = binding.loadingViewInclude.loadingView.apply { visibility = View.GONE }

}
