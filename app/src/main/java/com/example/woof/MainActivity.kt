/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.woof

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.example.woof.data.Dog
import com.example.woof.data.dogs
import com.example.woof.ui.theme.WoofTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WoofTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WoofApp()
                }
            }
        }
    }
}

/**
 * Composable utama yang mengatur Scaffold, TopBar, dan list anjing.
 * State untuk modal gambar disimpan di sini.
 */
@Composable
fun WoofApp() {
    // State untuk menyimpan resource ID gambar yang akan ditampilkan di modal.
    // Null berarti tidak ada modal yang ditampilkan.
    var selectedDogImage by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            WoofTopAppBar()
        }
    ) { it ->
        LazyColumn(contentPadding = it) {
            items(dogs) { dog ->
                DogItem(
                    dog = dog,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small)),
                    // Teruskan lambda untuk menangani klik pada ikon anjing
                    onIconClick = { imageResId ->
                        selectedDogImage = imageResId
                    }
                )
            }
        }
    }

    // Tampilkan modal jika selectedDogImage tidak null
    if (selectedDogImage != null) {
        DogImageModal(
            dogImageRes = selectedDogImage!!,
            onDismiss = { selectedDogImage = null }
        )
    }
}


/**
 * Composable untuk menampilkan modal gambar anjing.
 */
@Composable
fun DogImageModal(
    @DrawableRes dogImageRes: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium
        ) {
            Image(
                painter = painterResource(id = dogImageRes),
                contentDescription = null, // Gambar di sini hanya dekoratif
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_medium))
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}


/**
 * Composable untuk setiap item anjing.
 * State untuk expand/collapse disimpan di sini.
 *
 * @param dog data anjing
 * @param onIconClick lambda yang dipanggil saat ikon anjing diklik
 * @param modifier modifier untuk composable ini
 */
@Composable
fun DogItem(
    dog: Dog,
    modifier: Modifier = Modifier,
    onIconClick: (Int) -> Unit
) {
    // State untuk melacak apakah item ini sedang di-expand atau tidak.
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .animateContentSize( // Tambahkan animasi saat ukuran konten berubah
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                .clickable { expanded = !expanded } // Buat seluruh Column bisa diklik untuk toggle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_small))
            ) {
                // Panggil DogIcon dengan lambda onIconClick
                DogIcon(dog.imageResourceId, onIconClick = { onIconClick(dog.imageResourceId) })
                DogInformation(dog.name, dog.age)
                Spacer(Modifier.weight(1f))
                DogItemButton(
                    expanded = expanded,
                    onClick = { expanded = !expanded }
                )
            }
            // Tampilkan hobi jika item di-expand
            if (expanded) {
                DogHobby(
                    dog.hobbies,
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.padding_medium),
                        top = dimensionResource(R.dimen.padding_small),
                        end = dimensionResource(R.dimen.padding_medium),
                        bottom = dimensionResource(R.dimen.padding_medium)
                    )
                )
            }
        }
    }
}

/**
 * Composable untuk tombol expand/collapse (panah atas/bawah).
 */
@Composable
private fun DogItemButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = stringResource(R.string.expand_button_content_description),
            tint = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * Composable untuk menampilkan hobi anjing.
 */
@Composable
fun DogHobby(
    @StringRes dogHobby: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.about),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = stringResource(dogHobby),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun WoofTopAppBar(modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.image_size))
                        .padding(dimensionResource(R.dimen.padding_small)),
                    painter = painterResource(R.drawable.ic_woof_logo),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Modifikasi DogIcon agar bisa diklik.
 *
 * @param dogIcon resource ID gambar
 * @param onIconClick lambda yang akan dipanggil saat ikon diklik
 * @param modifier modifier untuk composable ini
 */
@Composable
fun DogIcon(
    @DrawableRes dogIcon: Int,
    modifier: Modifier = Modifier,
    onIconClick: () -> Unit
) {
    Image(
        modifier = modifier
            .size(dimensionResource(R.dimen.image_size))
            .padding(dimensionResource(R.dimen.padding_small))
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onIconClick), // Tambahkan modifier clickable
        contentScale = ContentScale.Crop,
        painter = painterResource(dogIcon),
        contentDescription = null
    )
}

@Composable
fun DogInformation(
    @StringRes dogName: Int,
    dogAge: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(dogName),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small))
        )
        Text(
            text = stringResource(R.string.years_old, dogAge),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview
@Composable
fun WoofPreview() {
    WoofTheme(darkTheme = false) {
        WoofApp()
    }
}

@Preview
@Composable
fun WoofDarkThemePreview() {
    WoofTheme(darkTheme = true) {
        WoofApp()
    }
}