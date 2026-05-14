package net.abaresults.progresspath.view.clinics

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
import net.abaresults.progresspath.BaseFragment
import net.abaresults.progresspath.R
import net.abaresults.progresspath.databinding.FragmentClinicsBinding
import net.abaresults.progresspath.model.Clinic

@AndroidEntryPoint
class ClinicsFragment : BaseFragment() {

    private lateinit var binding: FragmentClinicsBinding
    private val viewModel: ClinicsViewModel by viewModels()

    private lateinit var clinicsAdapter: ClinicsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClinicsBinding.inflate(inflater, container, false)
        observeData(viewModel)
        viewModel.takeAction(ClinicsAction.Start)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureViews()
    }

    private fun observeData(viewModel: ClinicsViewModel) {
        val stateObserver = Observer<ClinicsState?> {
            // null state indicates there is no action needed
            it ?: return@Observer

            // Hide the loading state
            if (it != ClinicsState.Loading) {
                hideLoading()
            }

            when (it) {
                is ClinicsState.Loading -> showLoading()
                is ClinicsState.Error -> showError(it.generalError)
                is ClinicsState.Idle -> {}
                is ClinicsState.ContentLoaded -> handleContentLoaded(it.clinics)
                is ClinicsState.GoToKids -> navigateToKids()
            }
        }
        viewModel.state.observe(viewLifecycleOwner, stateObserver)

        viewModel.userType.observe(viewLifecycleOwner) {
            binding.fabAddClinic.isVisible = false
        }
    }

    private fun configureViews() {
        showTopBar()
        showBottomBar()

        clinicsAdapter = ClinicsAdapter(
            onItemClicked = { clinic ->
                viewModel.takeAction(ClinicsAction.ClinicClicked(clinic))
            }
        )

        binding.clinicsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clinicsAdapter
        }

        binding.fabAddClinic.isVisible = false
    }

    private fun handleContentLoaded(clinics: List<Clinic>) {
        clinicsAdapter.updateData(clinics)
    }

    private fun showError(generalError: String) {
        binding.errorTextView.text = generalError
        binding.errorTextView.visibility = View.VISIBLE
    }

    private fun navigateToKids() {
        findNavController().navigate(R.id.kidsFragment)
    }

    private fun showLoading() =
        binding.loadingViewInclude.loadingView.apply { visibility = View.VISIBLE }

    private fun hideLoading() =
        binding.loadingViewInclude.loadingView.apply { visibility = View.GONE }

}
