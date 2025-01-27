package com.wafflestudio.siksha2.ui.restaurant_info

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wafflestudio.siksha2.R
import com.wafflestudio.siksha2.databinding.FragmentRestaurantInfoBinding
import com.wafflestudio.siksha2.models.RestaurantInfo
import com.wafflestudio.siksha2.utils.visibleOrGone
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

// TODO: 전반적으로 대충 짬 나중에 날잡고 고치기
@AndroidEntryPoint
class RestaurantInfoDialogFragment private constructor() :
    BottomSheetDialogFragment(),
    OnMapReadyCallback {

    private lateinit var binding: FragmentRestaurantInfoBinding
    private val restaurantInfo: RestaurantInfo by lazy {
        arguments?.getParcelable<RestaurantInfo>(
            RESTAURANT_INFO
        )!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestaurantInfoBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.map.onCreate(null)
        binding.map.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as BottomSheetDialog).behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.title.text = restaurantInfo.nameKr
        binding.subtitle.text = restaurantInfo.address?.replace("서울 관악구 관악로 1", "") ?: ""
        restaurantInfo.etc?.operatingHours?.let {
            setUpOperatingHour(it)
        }
        binding.closeButton.setOnClickListener { dismiss() }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        // Add a marker in Sydney and move the camera
        MapsInitializer.initialize(context)
        val position = LatLng(restaurantInfo.latitude ?: 0.0, restaurantInfo.longitude ?: 0.0)
        with(googleMap!!) {
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.5f))
            addMarker(MarkerOptions().position(position))
            mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun setUpOperatingHour(operatingHour: RestaurantInfo.OperatingHour) {
        val items = listOf(binding.timeLayoutWeekday, binding.timeLayoutSaturday, binding.timeLayoutHoliday)
        if (operatingHour.weekdays.isNotEmpty()) {
            items[0].apply {
                visibleOrGone(true)
                val operating = modifyOperatingHour(operatingHour.weekdays)
                operating.forEachIndexed { index, s ->
                    if (s.isNotBlank()) {
                        when (index) {
                            0 -> {
                                binding.layoutWeekdayBreakfast.visibleOrGone(true)
                                binding.textWeekdayBreakfastTime.text = s
                            }
                            1 -> {
                                binding.layoutWeekdayLunch.visibleOrGone(true)
                                binding.textWeekdayLunchTime.text = s
                            }
                            2 -> {
                                binding.layoutWeekdayDinner.visibleOrGone(true)
                                binding.textWeekdayDinnerTime.text = s
                            }
                        }
                    }
                }
            }
        }

        if (operatingHour.saturday.isNotEmpty()) {
            items[1].apply {
                visibleOrGone(true)
                binding.lineDivFirst.visibility = View.VISIBLE

                val operating = modifyOperatingHour(operatingHour.saturday)
                operating.forEachIndexed { index, s ->
                    if (s.isNotBlank()) {
                        when (index) {
                            0 -> {
                                binding.layoutSaturdayBreakfast.visibleOrGone(true)
                                binding.textSaturdayBreakfastTime.text = s
                            }
                            1 -> {
                                binding.layoutSaturdayLunch.visibleOrGone(true)
                                binding.textSaturdayLunchTime.text = s
                            }
                            2 -> {
                                binding.layoutSaturdayDinner.visibleOrGone(true)
                                binding.textSaturdayDinnerTime.text = s
                            }
                        }
                    }
                }
            }
        }

        if (operatingHour.holiday.isNotEmpty()) {
            items[2].apply {
                visibleOrGone(true)
                binding.lineDivSecond.visibility = View.VISIBLE

                val operating = modifyOperatingHour(operatingHour.holiday)
                operating.forEachIndexed { index, s ->
                    if (s.isNotBlank()) {
                        when (index) {
                            0 -> {
                                binding.layoutHolidayBreakfast.visibleOrGone(true)
                                binding.textHolidayBreakfastTime.text = s
                            }
                            1 -> {
                                binding.layoutHolidayLunch.visibleOrGone(true)
                                binding.textHolidayLunchTime.text = s
                            }
                            2 -> {
                                binding.layoutHolidayDinner.visibleOrGone(true)
                                binding.textHolidayDinnerTime.text = s
                            }
                        }
                    }
                }
            }
        }
    }

    private fun modifyOperatingHour(operatingHour: List<String>): List<String> {
        val mutableOperatingHour = operatingHour.toMutableList()
        val minuteList = mutableListOf<Pair<Int, Int>>()
        val resultList = mutableListOf<String>()
        operatingHour.forEach {
            val minList = it.split("-")
            var startMinute = 0
            minList[0].split(':').forEachIndexed { index, s -> when (index) { 0 -> startMinute += s.toInt() * 60 ; 1 -> startMinute += s.toInt() } }
            var endMinute = 0
            minList[1].split(':').forEachIndexed { index, s -> when (index) { 0 -> endMinute += s.toInt() * 60 ; 1 -> endMinute += s.toInt() } }
            minuteList.add(Pair(startMinute, endMinute))
        }

        for (i in 0..2) {
            when (i) {
                0 -> {
                    var isFound = false
                    minuteList.forEach {
                        if (it.first < BREAKFAST_TIME_AS_MINUTE && it.second > BREAKFAST_TIME_AS_MINUTE) {
                            resultList.add(mutableOperatingHour.removeAt(0))
                            isFound = true
                            return@forEach
                        }
                    }
                    if (isFound) {
                        minuteList.removeAt(0)
                        continue
                    } else resultList.add("")
                }
                1 -> {
                    var isFound = false
                    minuteList.forEach {
                        if (it.first < LUNCH_TIME_AS_MINUTE && it.second > LUNCH_TIME_AS_MINUTE) {
                            resultList.add(mutableOperatingHour.removeAt(0))
                            isFound = true
                            return@forEach
                        }
                    }
                    if (isFound) {
                        minuteList.removeAt(0)
                        continue
                    } else resultList.add("")
                }
                2 -> {
                    var isFound = false
                    minuteList.forEach {
                        if (it.first < DINNER_TIME_AS_MINUTE && it.second > DINNER_TIME_AS_MINUTE) {
                            resultList.add(mutableOperatingHour.removeAt(0))
                            isFound = true
                            return@forEach
                        }
                    }
                    if (isFound) {
                        minuteList.removeAt(0)
                        continue
                    } else resultList.add("")
                }
            }
        }

        return resultList
    }

    companion object {
        const val RESTAURANT_INFO = "restaurant_info"

        private const val BREAKFAST_TIME_AS_MINUTE = 510
        private const val LUNCH_TIME_AS_MINUTE = 750
        private const val DINNER_TIME_AS_MINUTE = 1080

        @JvmStatic
        fun newInstance(restaurantInfo: RestaurantInfo) =
            RestaurantInfoDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RESTAURANT_INFO, restaurantInfo)
                }
            }
    }
}
