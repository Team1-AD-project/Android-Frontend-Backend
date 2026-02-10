package com.ecogo.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.api.NextBusApiClient
import com.ecogo.data.BusRoute
import com.ecogo.databinding.FragmentRoutesBinding
import com.ecogo.ui.adapters.BusRouteAdapter
import kotlinx.coroutines.launch

class RoutesFragment : Fragment() {

    private var _binding: FragmentRoutesBinding? = null
    private val binding get() = _binding!!

    private var routeDescMap: Map<String, String> = emptyMap()


    // ====== 站点：全量写死（从你 /BusStops 返回抄来的） ======
    data class BusStopOption(val code: String, val label: String)

    private val allStops: List<BusStopOption> = listOf(
        BusStopOption("PGP", "Prince George's Park (PGP)"),
        BusStopOption("HSSML-OPP", "Opp Hon Sui Sen Memorial Library (Opp HSSML)"),
        BusStopOption("NUSS-OPP", "Opp NUSS"),
        BusStopOption("LT13-OPP", "Ventus (Opp LT13)"),
        BusStopOption("IT", "Information Technology (IT)"),
        BusStopOption("YIH-OPP", "Opp Yusof Ishak House (Opp YIH)"),
        BusStopOption("UTOWN", "University Town (UTown)"),
        BusStopOption("RAFFLES", "Raffles Hall"),
        BusStopOption("KV", "Kent Vale (KV)"),
        BusStopOption("MUSEUM", "Museum"),
        BusStopOption("YIH", "Yusof Ishak House (YIH)"),
        BusStopOption("CLB", "Central Library (CLB)"),
        BusStopOption("LT13", "LT 13"),
        BusStopOption("AS5", "AS 5"),
        BusStopOption("BIZ2", "BIZ 2"),
        BusStopOption("CG", "College Green"),
        BusStopOption("OTH", "Oei Tiong Ham Building (OTH)"),
        BusStopOption("BG-MRT", "Botanic Gardens MRT (BG MRT)"),
        BusStopOption("KR-MRT", "Kent Ridge MRT (KR MRT)"),
        BusStopOption("UHC-OPP", "Opp University Health Centre (Opp UHC)"),
        BusStopOption("LT27", "LT 27"),
        BusStopOption("UHALL", "University Hall (UHall)"),
        BusStopOption("SDE3-OPP", "Opp SDE 3"),
        BusStopOption("JP-SCH-16151", "The Japanese Primary School"),
        BusStopOption("UHC", "University Health Centre (UHC)"),
        BusStopOption("UHALL-OPP", "Opp University Hall (Opp UHall)"),
        BusStopOption("S17", "S 17"),
        BusStopOption("KR-MRT-OPP", "Opp Kent Ridge MRT (Opp KR MRT)"),
        BusStopOption("PGPR", "Prince George's Park Foyer (PGP Foyer)"),
        BusStopOption("COM3", "COM 3"),
        BusStopOption("TCOMS-OPP", "Opp TCOMS"),
        BusStopOption("TCOMS", "TCOMS"),
        BusStopOption("KRB", "Kent Ridge Bus Terminal (KR Bus Ter)")
    )

    private var selectedStop: BusStopOption = allStops.firstOrNull { it.code == "UTOWN" } ?: allStops.first()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAnimations()
        setupBusStopSpinner()

        // ✅ 默认加载 TCOMS
        loadRoutes(selectedStop)
    }

    private fun setupRecyclerView() {
        binding.recyclerRoutes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = BusRouteAdapter(emptyList()) { route ->
                handleRouteClick(route)
            }
        }
    }

    private fun setupBusStopSpinner() {
        val labels = allStops.map { it.label }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBusStop.adapter = adapter

        // 默认选中 TCOMS
        val defaultIndex = allStops.indexOfFirst { it.code == selectedStop.code }.coerceAtLeast(0)
        binding.spinnerBusStop.setSelection(defaultIndex, false)

        binding.spinnerBusStop.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newStop = allStops.getOrNull(position) ?: return
                if (newStop.code == selectedStop.code) return
                selectedStop = newStop
                saveSelectedStop(newStop)
                loadRoutes(selectedStop)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveSelectedStop(stop: BusStopOption) {
        val sp = requireContext().getSharedPreferences("nextbus_pref", android.content.Context.MODE_PRIVATE)
        sp.edit()
            .putString("stop_code", stop.code)
            .putString("stop_label", stop.label)
            .apply()
    }


    private fun loadRoutes(stop: BusStopOption) {
        viewLifecycleOwner.lifecycleScope.launch {
            val routes = runCatching {
                ensureRouteDescriptionsLoaded()

                val resp = NextBusApiClient.api.getShuttleService(stop.code)
                mapToBusRoutes(stop, resp)
            }.getOrElse {
                // 不做 empty / loading：失败就显示空列表或保留旧列表
                emptyList()
            }

            binding.recyclerRoutes.adapter = BusRouteAdapter(routes) { route ->
                handleRouteClick(route)
            }
        }
    }

    private suspend fun ensureRouteDescriptionsLoaded() {
        if (routeDescMap.isNotEmpty()) return

        routeDescMap = runCatching {
            val resp = NextBusApiClient.api.getServiceDescription()
            val list = resp.ServiceDescriptionResult?.ServiceDescription.orEmpty()

            list.mapNotNull { item ->
                val route = item.Route?.trim()
                val desc = item.RouteDescription?.trim()
                if (route.isNullOrEmpty() || desc.isNullOrEmpty()) null else route to desc
            }.toMap()
        }.getOrElse {
            emptyMap()
        }
    }


    /**
     * ✅ 关键：ShuttleService 动态路线 → BusRoute 列表
     * - route.name: shuttle.name (D1/K/R1/...)
     * - nextArrival: _etas[0].eta
     * - status: ETA 推断
     * - from/to: 默认值（from=站点名，to="-"）
     * - color: routeName hash 到固定颜色
     */
    private fun mapToBusRoutes(
        stop: BusStopOption,
        resp: com.ecogo.api.ShuttleServiceResponse
    ): List<BusRoute> {
        val shuttles = resp.ShuttleServiceResult?.shuttles.orEmpty()

        val list = shuttles.mapNotNull { shuttle ->
            val routeName = shuttle.name?.trim().orEmpty()
            if (routeName.isEmpty()) return@mapNotNull null

            val etaMinutes = shuttle._etas?.firstOrNull()?.eta
            val etaSafe = etaMinutes ?: -1

            // ✅ 用 ServiceDescription 的 RouteDescription 拆 from/to
            val routeDesc = routeDescMap[routeName]
            val brief = formatBriefRoute(routeDesc, "${stop.label}")



            BusRoute(
                id = null,
                name = routeName,
                from = brief,
                to = "",
                color = colorForRoute(routeName),
                status = statusFromEta(etaSafe),
                time = if (etaSafe >= 0) "$etaSafe min" else "-",
                crowd = null,
                crowding = "",
                nextArrival = if (etaSafe >= 0) etaSafe else 0,
                operational = true
            )
        }

        // ✅ 没有 ETA 的排最后（避免 -1 排到最前）
        return list.sortedBy { if (it.nextArrival <= 0) Int.MAX_VALUE else it.nextArrival }
    }

    private fun formatBriefRoute(routeDesc: String?, fallback: String): String {
        if (routeDesc.isNullOrBlank()) return fallback

        val parts = routeDesc.split(">").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return fallback

        val maxNodes = 6
        val nodes = if (parts.size <= maxNodes) {
            parts
        } else {
            // 前4 + … + 最后1
            parts.take(4) + listOf("…") + parts.takeLast(1)
        }

        // ✅ 关键：只用 join，不要额外 append 分隔符
        return nodes.joinToString(" → ")
    }





    private fun statusFromEta(eta: Int): String {
        if (eta < 0) return "On Time"
        return when {
            eta <= 2 -> "Arriving"
            eta <= 8 -> "On Time"
            eta <= 30 -> "Delayed"
            else -> "Scheduled"
        }
    }


    // ✅ 稳定颜色：不同路线(D1/K/R1/...)也能有不同固定色
    private fun colorForRoute(routeName: String): String {
        val palette = listOf(
            "#2563EB", // blue
            "#16A34A", // green
            "#7C3AED", // purple
            "#F97316", // orange
            "#DC2626", // red
            "#0EA5E9", // sky
            "#059669", // emerald
            "#9333EA"  // violet
        )
        val idx = (routeName.hashCode().absoluteValue) % palette.size
        return palette[idx]
    }

    private val Int.absoluteValue: Int get() = if (this < 0) -this else this

    private fun handleRouteClick(route: BusRoute) {
        // 维持你现有逻辑
        findNavController().navigate(R.id.action_routes_to_routePlanner)
    }

    private fun setupAnimations() {
        val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        binding.recyclerRoutes.startAnimation(slideUp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
