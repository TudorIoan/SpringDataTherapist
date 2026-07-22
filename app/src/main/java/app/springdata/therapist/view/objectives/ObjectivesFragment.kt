package app.springdata.therapist.view.objectives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import app.springdata.therapist.BaseFragment
import app.springdata.therapist.R
import app.springdata.therapist.databinding.FragmentObjectivesBinding

@AndroidEntryPoint
class ObjectivesFragment : BaseFragment() {

    private lateinit var binding: FragmentObjectivesBinding
    private val viewModel: ObjectivesViewModel by viewModels()

    private lateinit var objectivesAdapter: ObjectivesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentObjectivesBinding.inflate(inflater, container, false)
        observeData(viewModel)
        viewModel.takeAction(ObjectivesAction.Start)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureViews()
    }

    private fun observeData(viewModel: ObjectivesViewModel) {
        val stateObserver = Observer<ObjectivesState?> {
            // null state indicates there is no action needed
            it ?: return@Observer

            // Hide the loading state
            if (it != ObjectivesState.Loading) {
                hideLoading()
            }

            when (it) {
                is ObjectivesState.Loading -> showLoading()
                is ObjectivesState.Error -> showError(it.generalError)
                is ObjectivesState.Idle -> {}
                is ObjectivesState.ContentLoaded -> handleContentLoaded(it.items)
                is ObjectivesState.GoToItems -> navigateToItems()
            }
        }
        viewModel.state.observe(viewLifecycleOwner, stateObserver)

        viewModel.title.observe(viewLifecycleOwner) { title ->
            val titleParts = title.split(" > ", limit = 2)
            val clinicName = titleParts.firstOrNull().orEmpty()
            val kidName = titleParts.getOrNull(1).orEmpty()

            setTitle(kidName.ifEmpty { getString(R.string.objectives) })
            binding.subtitleInclude.subtitleTextView.isVisible = true
            binding.subtitleInclude.subtitleTextView.text = getString(R.string.clinic_title, clinicName)
            binding.subtitleInclude.sectionTitleTextView.isVisible = true
            binding.subtitleInclude.sectionTitleTextView.text = getString(R.string.objectives)
        }

        viewModel.userType.observe(viewLifecycleOwner) { userType ->
            binding.fabAddObjective.isVisible = false
        }
    }

    private fun configureViews() {
        hideBottomBar()

        objectivesAdapter = ObjectivesAdapter(
            onItemClicked = { objectiveListItem ->
                viewModel.takeAction(ObjectivesAction.ObjectiveClicked(objectiveListItem))
            }
        )

        binding.objectivesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = objectivesAdapter
        }

        binding.fabAddObjective.isVisible = false
    }

    private fun navigateToItems() {
        findNavController().navigate(R.id.itemsFragment)
    }

    private fun handleContentLoaded(objectives: List<ObjectivesListItem>) {
        objectivesAdapter.set(objectives)
    }

    private fun showError(generalError: String) {
        binding.subtitleInclude.errorTextView.text = generalError
        binding.subtitleInclude.errorTextView.visibility = View.VISIBLE
    }

    private fun showLoading() = binding.loadingViewInclude.loadingView.apply { visibility = View.VISIBLE }

    private fun hideLoading() = binding.loadingViewInclude.loadingView.apply { visibility = View.GONE }

}
