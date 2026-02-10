package com.ecogo.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.auth.TokenManager
import com.ecogo.databinding.FragmentTripHistoryBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.TripHistoryAdapter
import com.ecogo.data.TripMapper
import kotlinx.coroutines.launch

class TripHistoryFragment : Fragment() {

    private var _binding: FragmentTripHistoryBinding? = null
    private val binding get() = _binding!!

    private val repo = EcoGoRepository()
    private lateinit var adapter: TripHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        loadTripsFromApi()
    }

    private fun setupRecycler() {
        adapter = TripHistoryAdapter(emptyList())
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TripHistoryFragment.adapter
        }
    }

    private fun loadTripsFromApi() {
        // ✅ mobile 接口是 authenticated：只要 token 就行，不需要 userId path
        if (TokenManager.getToken().isNullOrBlank()) {
            showEmpty()
            return
        }

        showLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("TRIP_HISTORY", "auth=${TokenManager.getAuthHeader()}")

                // ✅ 关键：改成你新加的 mobile 接口
                val trips = repo.getMyTripHistory().getOrThrow()

                val uiList = trips
                    .sortedByDescending { it.startTime ?: it.createdAt ?: "" }
                    .map { TripMapper.toSummary(it) }

                adapter.update(uiList)

                if (uiList.isEmpty()) showEmpty() else showContent()
            } catch (e: Exception) {
                Log.e("TRIP_HISTORY", "load trips failed", e)
                showEmpty()
            } finally {
                hideLoading()
            }
        }
    }

    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        binding.recyclerHistory.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressLoading.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.recyclerHistory.visibility = View.GONE
    }

    private fun showContent() {
        binding.layoutEmpty.visibility = View.GONE
        binding.recyclerHistory.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
