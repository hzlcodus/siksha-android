package com.wafflestudio.siksha2.ui.menu_detail

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.wafflestudio.siksha2.models.Menu
import com.wafflestudio.siksha2.models.Review
import com.wafflestudio.siksha2.repositories.MenuRepository
import com.wafflestudio.siksha2.utils.PathUtil
import com.wafflestudio.siksha2.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MenuDetailViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _menu = MutableLiveData<Menu>()
    val menu: LiveData<Menu>
        get() = _menu

    private val _commentHint = MutableLiveData<String>()
    val commentHint: LiveData<String>
        get() = _commentHint

    private val _networkResultState = MutableLiveData<State>()
    val networkResultState: LiveData<State>
        get() = _networkResultState

    private val _reviewDistribution = MutableLiveData<List<Long>>()
    val reviewDistribution: LiveData<List<Long>>
        get() = _reviewDistribution

    private val _uriList = MutableLiveData<List<Uri>>()
    val uriList: LiveData<List<Uri>>
        get() = _uriList

    private val _imageUrlList = MutableLiveData<List<String>>()
    val imageUrlList: LiveData<List<String>>
        get() = _imageUrlList

    private val _imageCount = MutableLiveData<Long>(0)
    val imageCount: LiveData<Long>
        get() = _imageCount

    private val _leaveReviewState = MutableLiveData<ReviewState>(ReviewState.WAITING)
    val leaveReviewState: LiveData<ReviewState>
        get() = _leaveReviewState

    fun refreshMenu(menuId: Long) {
        _networkResultState.value = State.LOADING
        viewModelScope.launch {
            try {
                _menu.value = menuRepository.getMenuById(menuId)
                _networkResultState.value = State.SUCCESS
            } catch (e: IOException) {
                _networkResultState.value = State.FAILED
            }
        }
    }

    fun refreshImages(menuId: Long) {
        viewModelScope.launch {
            try {
                val data = menuRepository.getFirstReviewPhotoByMenuId(menuId)
                _imageCount.value = data.totalCount
                val urlList = emptyList<String>().toMutableList()
                for (i in 0 until 3) {
                    if (i < data.result.size) {
                        data.result[i].etc?.images?.get(0)?.let {
                            urlList.add(it)
                        }
                    }
                }
                _imageUrlList.value = urlList
            } catch (e: IOException) {
                _imageUrlList.value = emptyList()
                _networkResultState.value = State.FAILED
            }
        }
    }

    fun getReviews(menuId: Long): Flow<PagingData<Review>> {
        return menuRepository.getPagedReviewsByMenuIdFlow(menuId)
    }

    fun getReviewsWithImages(menuId: Long): Flow<PagingData<Review>> {
        return menuRepository.getPagedReviewsOnlyHaveImagesByMenuIdFlow(menuId)
    }

    fun getRecommendationReview(score: Long) {
        // TODO: LruCache 로 캐싱해놓고 꺼내쓰기
        viewModelScope.launch {
            try {
                _commentHint.value = menuRepository.getReviewRecommendationComments(score)
            } catch (e: IOException) {
                _commentHint.value = ""
            }
        }
    }

    fun refreshReviewDistribution(menuId: Long) {
        viewModelScope.launch {
            try {
                _reviewDistribution.value = menuRepository.getReviewDistribution(menuId)
            } catch (e: IOException) {
                _reviewDistribution.value = emptyList()
            }
        }
    }

    fun addUri(uri: Uri): Boolean {
        val list = _uriList.value?.toMutableList() ?: mutableListOf()
        if (list.size >= 3) return false
        list.add(uri)
        _uriList.value = list.toList()
        return true
    }

    fun deleteUri(index: Int): Boolean {
        val list = _uriList.value?.toMutableList() ?: mutableListOf()
        if (list.size < index + 1) return false
        list.removeAt(index)
        _uriList.value = list.toList()
        return true
    }

    fun refreshUriList() {
        _uriList.value = listOf()
    }

    fun notifySendReviewEnd() {
        _leaveReviewState.value = ReviewState.WAITING
    }

    suspend fun leaveReview(context: Context, score: Double, comment: String) {
        _menu.value?.id?.let { id ->
            if (_uriList.value?.isNotEmpty() == true) {
                context.showToast("이미지 압축 중입니다.")
                _leaveReviewState.value = ReviewState.COMPRESSING
                val imageList = mutableListOf<MultipartBody.Part>()
                _uriList.value?.forEach {
                    val path = PathUtil.getPath(context, it)
                    var file = File(path)
                    file = Compressor.compress(context, file) {
                        resolution(300, 300)
                        size(100000)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val multipartBody = MultipartBody.Part.createFormData("images", file.name, requestBody)
                    imageList.add(multipartBody)
                }
                val commentBody = MultipartBody.Part.createFormData("comment", comment)
                menuRepository.leaveMenuReviewImage(id, score.toLong(), commentBody, imageList)
            } else {
                menuRepository.leaveMenuReview(id, score, comment)
            }
        }
    }

    enum class State {
        LOADING,
        SUCCESS,
        FAILED
    }

    enum class ReviewState {
        WAITING,
        COMPRESSING
    }
}
