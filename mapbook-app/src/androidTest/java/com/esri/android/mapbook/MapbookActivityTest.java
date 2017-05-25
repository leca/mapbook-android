package com.esri.android.mapbook;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.esri.android.mapbook.MapBookApplication;
import com.esri.android.mapbook.R;
import com.esri.android.mapbook.data.FileManager;
import com.esri.android.mapbook.mapbook.MapbookActivity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */
@RunWith(AndroidJUnit4.class)
public class MapbookActivityTest {

  @Rule public final ActivityTestRule<MapbookActivity> main =
      new ActivityTestRule<MapbookActivity>(MapbookActivity.class, true,false);

  @Inject FileManager fileManager;

  @Before
  public void setUp() {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    MapBookApplication app
        = (MapBookApplication) instrumentation.getTargetContext().getApplicationContext();
    TestComponent component = (TestComponent) app.createComponent();
    component.inject(this);
  }

  @Test
  public void shouldBeAbleToLaunchMainScreen(){
    main.launchActivity(new Intent());
    onView(withText(main.getActivity().getString(R.string.title))).check(ViewAssertions.matches(isDisplayed()));
    Assert.assertTrue(fileManager != null);
    Mockito.when(fileManager.fileExists()).thenReturn(null);
    Assert.assertNull(fileManager.fileExists());
  }
}
