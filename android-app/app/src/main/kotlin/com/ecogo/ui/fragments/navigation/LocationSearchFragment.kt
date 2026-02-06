package com.ecogo.ui.fragments.navigation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.data.MockData
import com.ecogo.data.NavLocation
import com.ecogo.databinding.FragmentLocationSearchBinding
import com.ecogo.ui.adapters.LocationAdapter
import com.ecogo.viewmodel.NavigationViewModel

/**
 * 地点搜索Fragment
 */
class LocationSearchFragment : Fragment() {

    private var _binding: FragmentLocationSearchBinding? = null
    private val binding get() = _binding!!
    
    private val args: LocationSearchFragmentArgs by navArgs()
    private lateinit var viewModel: NavigationViewModel
    private lateinit var locationAdapter: LocationAdapter
    
    private var allLocations = MockData.CAMPUS_LOCATIONS
    private var isSelectingOrigin = true // true=选择起点, false=选择终点

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isSelectingOrigin = args.isSelectingOrigin
        viewModel = ViewModelProvider(requireActivity())[NavigationViewModel::class.java]
        
        setupRecyclerView()
        setupSearchBar()
        setupUI()
        
        // 显示所有地点
        showAllLocations()
    }
    
    private fun setupRecyclerView() {
        locationAdapter = LocationAdapter { location ->
            onLocationSelected(location)
        }
        
        binding.recyclerLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = locationAdapter
        }
    }
    
    private fun setupSearchBar() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLocations(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // 标题已在布局中设置
    }
    
    private fun showAllLocations() {
        // 按访问次数排序（常用地点在前）
        val sortedLocations = allLocations.sortedByDescending { it.visitCount }
        locationAdapter.updateLocations(sortedLocations)
        
        binding.emptyState.visibility = View.GONE
        binding.recyclerLocations.visibility = View.VISIBLE
    }
    
    private fun filterLocations(query: String) {
        if (query.isBlank()) {
            showAllLocations()
            return
        }
        
        val filtered = allLocations.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }
        
        if (filtered.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerLocations.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerLocations.visibility = View.VISIBLE
            locationAdapter.updateLocations(filtered)
        }
    }
    
    private fun onLocationSelected(location: NavLocation) {
        if (isSelectingOrigin) {
            viewModel.setOrigin(location)
        } else {
            viewModel.setDestination(location)
        }
        
        // 返回到上一个界面
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
