package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.auth.TokenManager
import com.ecogo.data.OrderSummaryUi
import com.ecogo.databinding.FragmentOrderHistoryBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.OrderHistoryAdapter
import kotlinx.coroutines.launch

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private val repo = EcoGoRepository()
    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadOrdersFromApi()   // ✅ 默认走真实接口
        // loadOrdersMock()    // (需要时再打开)
    }

    private fun setupRecycler() {
        adapter = OrderHistoryAdapter(emptyList())
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OrderHistoryFragment.adapter
        }
    }

    private fun loadOrdersFromApi() {
        val userId = TokenManager.getUserId() ?: run {
            showEmpty()
            return
        }

        showLoading()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val data = repo
                    .getUserOrderHistoryMobile(userId = userId, status = null, page = 1, size = 20)
                    .getOrThrow()

                val list = data.orders

                android.util.Log.d("HISTORY", "parsed orders size = ${list.size}")

                adapter.update(list)

                if (list.isEmpty()) showEmpty() else showContent()

            } catch (e: Exception) {
                android.util.Log.e("HISTORY", "load orders failed", e)
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
