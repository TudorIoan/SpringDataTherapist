package app.springdata.therapist.view.home

import app.springdata.therapist.api.model.UnsplashPhoto

data class HomeStateData (
    val photos: List<UnsplashPhoto> = listOf()
)