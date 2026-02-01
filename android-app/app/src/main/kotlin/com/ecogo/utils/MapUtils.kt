package com.ecogo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ecogo.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 地图工具类
 * 提供地图相关的工具方法
 */
object MapUtils {
    
    /**
     * 创建自定义标记图标
     * @param context Context
     * @param drawableId 图标资源ID
     * @return BitmapDescriptor
     */
    fun bitmapDescriptorFromVector(context: Context, drawableId: Int): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        vectorDrawable?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(
                it.intrinsicWidth,
                it.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            it.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
        return null
    }
    
    /**
     * 设置地图样式
     * @param map GoogleMap对象
     * @param context Context
     */
    fun setMapStyle(map: GoogleMap, context: Context) {
        try {
            // 可以加载自定义地图样式JSON
            // val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
            // if (!success) {
            //     Log.e("MapUtils", "Style parsing failed.")
            // }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 绘制路线
     * @param map GoogleMap对象
     * @param polylineString 编码的路线字符串
     * @param color 路线颜色
     * @param width 路线宽度
     * @return Polyline对象
     */
    fun drawRoute(
        map: GoogleMap,
        polylineString: String,
        color: Int,
        width: Float = 10f
    ): Polyline? {
        if (polylineString.isEmpty()) return null
        
        val points = decodePolyline(polylineString)
        return map.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(color)
                .width(width)
                .geodesic(true)
        )
    }
    
    /**
     * 解码Google polyline字符串
     * @param encoded 编码的字符串
     * @return LatLng列表
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }
    
    /**
     * 计算两个经纬度之间的距离（米）
     * @param lat1 起点纬度
     * @param lon1 起点经度
     * @param lat2 终点纬度
     * @param lon2 终点经度
     * @return 距离（米）
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // 地球半径（米）
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * 相机动画移动到边界
     * @param map GoogleMap对象
     * @param points 点列表
     * @param padding 内边距（像素）
     */
    fun animateCameraToBounds(map: GoogleMap, points: List<LatLng>, padding: Int = 100) {
        if (points.isEmpty()) return
        
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        
        val bounds = builder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map.animateCamera(cameraUpdate)
    }
    
    /**
     * 格式化距离显示
     * @param meters 距离（米）
     * @return 格式化字符串
     */
    fun formatDistance(meters: Double): String {
        return when {
            meters >= 1000 -> String.format("%.1fkm", meters / 1000)
            else -> String.format("%.0fm", meters)
        }
    }
    
    /**
     * 格式化时间显示
     * @param minutes 分钟数
     * @return 格式化字符串
     */
    fun formatDuration(minutes: Int): String {
        return when {
            minutes >= 60 -> {
                val hours = minutes / 60
                val mins = minutes % 60
                "${hours}小时${mins}分钟"
            }
            else -> "${minutes}分钟"
        }
    }
    
    /**
     * 获取交通方式图标
     * @param mode 交通方式
     * @return 图标emoji
     */
    fun getModeIcon(mode: String): String {
        return when (mode.lowercase()) {
            "walk", "walking" -> "🚶"
            "cycle", "cycling", "bicycle" -> "🚲"
            "bus", "transit" -> "🚌"
            "car", "driving" -> "🚗"
            else -> "📍"
        }
    }
}
