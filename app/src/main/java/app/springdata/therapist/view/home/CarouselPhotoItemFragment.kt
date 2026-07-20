package app.springdata.therapist.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.springdata.therapist.api.model.UnsplashPhoto
import com.bumptech.glide.Glide
import app.springdata.therapist.databinding.CarouselPhotoItemBinding

class CarouselPhotoItemFragment(private val content: UnsplashPhoto) : Fragment() {

    private lateinit var binding: CarouselPhotoItemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CarouselPhotoItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content?.let {
            Glide.with(this).load(content.urls.small).into(binding.itemImg);
        }
    }
}