package com.example.presentation.peripheral.fragment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.presentation.R

/*
fun MovieBookingScreen() {
    Surface(color = Color.White) {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize(),
        ) {
            val (
                menuButton, coverImage, titleText, genreText, ratingText,
                castText, castContainer, castImage1, castImage2, castImage3, castImage4,
                descText, bottomSurface, bookButton, cinemaNameContainer,
            ) = createRefs()

            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)
                    .constrainAs(menuButton) {
                        start.linkTo(parent.start, 16.dp)
                        top.linkTo(parent.top, 16.dp)
                    }
            )

            val rightGuideline = createGuidelineFromStart(0.4f)

            Image(
                painter = painterResource(id = R.drawable.cover_image), contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .constrainAs(coverImage) {
                        start.linkTo(parent.start, 24.dp)
                        top.linkTo(menuButton.bottom, 16.dp)
                        end.linkTo(rightGuideline, 16.dp)
                        width = Dimension.fillToConstraints
                    }
                    .aspectRatio(2f / 3f),
            )

            Text(
                text = "Deadpool",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.constrainAs(titleText) {
                    top.linkTo(menuButton.bottom, 8.dp)
                    start.linkTo(coverImage.end, 16.dp)
                }
            )

            Text(
                text = "Action | 1h 48m",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.constrainAs(genreText) {
                    top.linkTo(titleText.bottom)
                    start.linkTo(coverImage.end, 16.dp)
                }
            )

            Text(
                text = "IMDb 8.0/10",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.constrainAs(ratingText) {
                    top.linkTo(genreText.bottom, 16.dp)
                    start.linkTo(coverImage.end, 16.dp)
                }
            )

            Text(
                text = "CAST",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.constrainAs(castText) {
                    bottom.linkTo(castContainer.top)
                    start.linkTo(coverImage.end, 16.dp)
                }
            )

            ConstraintLayout(modifier = Modifier.constrainAs(castContainer) {
                bottom.linkTo(coverImage.bottom)
                start.linkTo(coverImage.end, 16.dp)
                end.linkTo(parent.end, 16.dp)
                width = Dimension.fillToConstraints
            })
            {
                Image(
                    painter = painterResource(id = R.drawable.cast1),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(50.dp)
                        .constrainAs(castImage1) {
                            bottom.linkTo(coverImage.bottom)
                            start.linkTo(coverImage.end, 16.dp)
                        }
                        .aspectRatio(1f)
                )

                Image(
                    painter = painterResource(id = R.drawable.cast2),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(50.dp)
                        .constrainAs(castImage2) {
                            top.linkTo(castImage1.top)
                            bottom.linkTo(castImage1.bottom)
                            start.linkTo(castImage1.end)
                        }
                        .aspectRatio(1f)
                )

                Image(
                    painter = painterResource(id = R.drawable.cast3),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(50.dp)
                        .constrainAs(castImage3) {
                            top.linkTo(castImage1.top)
                            bottom.linkTo(castImage1.bottom)
                            start.linkTo(castImage2.end)
                        }
                        .aspectRatio(1f)
                )

                Box(modifier = Modifier
                    .height(50.dp)
                    .background(color = colorResource(id = R.color.gray))
                    .constrainAs(castImage4) {
                        top.linkTo(castImage1.top)
                        bottom.linkTo(castImage1.bottom)
                        start.linkTo(castImage3.end)
                        end.linkTo(parent.end, 16.dp)
                    }
                    .aspectRatio(1f)) {
                    Text(
                        text = "+9",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .align(Alignment.Center),
                        color = Color.Black
                    )
                }

                createHorizontalChain(
                    castImage1, castImage2, castImage3, castImage4,
                    chainStyle = ChainStyle.SpreadInside
                )
            }

            val barrier = createBottomBarrier(coverImage, castContainer)

            Text(
                text = stringResource(id = R.string.movie_info),
                color = Color(0x8A000000),
                fontSize = 16.sp,
                modifier = Modifier.constrainAs(descText) {
                    top.linkTo(barrier, 36.dp)
                    start.linkTo(parent.start, 24.dp)
                    end.linkTo(parent.end, 24.dp)
                    width = Dimension.preferredWrapContent
                }
            )

            Surface(color = colorResource(id = R.color.gray),
                modifier = Modifier.constrainAs(bottomSurface) {
                    top.linkTo(descText.bottom, 36.dp)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
            ) {}


            val (date1, date2, date3, date4, date5, date6, date7,
                day1, day2, day3, day4, day5, day6, day7, dateSelector, dateMarker) = createRefs()

            val selectedDateStartState = remember { mutableStateOf(day1.start) }
            val selectedDateEndState = remember { mutableStateOf(day1.end) }

            Surface(
                color = Color.White,
                modifier = Modifier
                    .constrainAs(dateSelector) {
                        top.linkTo(descText.bottom, 36.dp)
                        start.linkTo(selectedDateStartState.value)
                        end.linkTo(selectedDateEndState.value)
                        bottom.linkTo(day1.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    }
            ) {}

            Surface(
                color = Color.Black,
                modifier = Modifier
                    .height(4.dp)
                    .constrainAs(dateMarker) {
                        top.linkTo(dateSelector.top)
                        start.linkTo(dateSelector.start)
                        end.linkTo(dateSelector.end)
                        width = Dimension.fillToConstraints
                    }
            ) {}

            Text(
                text = "20",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date1) {
                        top.linkTo(descText.bottom, 52.dp)
                        start.linkTo(day1.start)
                        end.linkTo(day1.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day1.start
                        selectedDateEndState.value = day1.end
                    }
            )

            Text(
                text = "21",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date2) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day2.start)
                        end.linkTo(day2.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day2.start
                        selectedDateEndState.value = day2.end
                    }
            )

            Text(
                text = "22",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date3) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day3.start)
                        end.linkTo(day3.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day3.start
                        selectedDateEndState.value = day3.end
                    }
            )

            Text(
                text = "23",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date4) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day4.start)
                        end.linkTo(day4.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day4.start
                        selectedDateEndState.value = day4.end
                    }
            )

            Text(
                text = "24",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date5) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day5.start)
                        end.linkTo(day5.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day5.start
                        selectedDateEndState.value = day5.end
                    }
            )

            Text(
                text = "25",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date6) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day6.start)
                        end.linkTo(day6.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day6.start
                        selectedDateEndState.value = day6.end
                    }
            )

            Text(
                text = "26",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp,
                modifier = Modifier
                    .constrainAs(date7) {
                        top.linkTo(date1.top)
                        bottom.linkTo(date1.bottom)
                        start.linkTo(day7.start)
                        end.linkTo(day7.end)
                    }
                    .clickable {
                        selectedDateStartState.value = day7.start
                        selectedDateEndState.value = day7.end
                    }
            )


            Text(
                text = "SUN",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day1) {
                        top.linkTo(date1.bottom, 16.dp)
                    }
                    .clickable {
                        selectedDateStartState.value = day1.start
                        selectedDateEndState.value = day1.end
                    }
            )

            Text(
                text = "MON",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day2) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day2.start
                        selectedDateEndState.value = day2.end
                    }
            )

            Text(
                text = "TUE",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day3) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day3.start
                        selectedDateEndState.value = day3.end
                    }
            )

            Text(
                text = "WED",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day4) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day4.start
                        selectedDateEndState.value = day4.end
                    }
            )

            Text(
                text = "THU",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day5) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day5.start
                        selectedDateEndState.value = day5.end
                    }
            )

            Text(
                text = "FRI",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day6) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day6.start
                        selectedDateEndState.value = day6.end
                    }
            )

            Text(
                text = "SAT",
                style = MaterialTheme.typography.body1,
                color = colorResource(id = R.color.dark_grey),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .constrainAs(day7) {
                        top.linkTo(day1.top)
                        bottom.linkTo(day1.bottom)
                    }
                    .clickable {
                        selectedDateStartState.value = day7.start
                        selectedDateEndState.value = day7.end
                    }
            )

            createHorizontalChain(day1, day2, day3, day4, day5, day6, day7)

            ConstraintLayout(modifier = Modifier.constrainAs(cinemaNameContainer) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(dateSelector.bottom, 16.dp)
                bottom.linkTo(bookButton.top, 16.dp)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            })
            {
                val (cinemaNameText, cinemaDistText) = createRefs()

                Text(
                    text = "Velocity Cinema",
                    fontSize = 22.sp,
                    color = Color(0xDD000000),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.constrainAs(cinemaNameText) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                Text(
                    text = "4.6 km",
                    fontSize = 14.sp,
                    color = Color(0x8A000000),
                    modifier = Modifier.constrainAs(cinemaDistText) {
                        start.linkTo(cinemaNameText.start)
                        end.linkTo(cinemaNameText.end)
                        width = Dimension.fillToConstraints
                    }
                )

                createVerticalChain(cinemaNameText, cinemaDistText, chainStyle = ChainStyle.Packed)
            }


            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.brick_red)),
                modifier = Modifier
                    .height(60.dp)
                    .background(color = colorResource(id = R.color.brick_red))
                    .constrainAs(bookButton) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            ) {
                Text(
                    text = "BOOK TICKETS",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}*/
