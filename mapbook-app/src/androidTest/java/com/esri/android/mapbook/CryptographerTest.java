package com.esri.android.mapbook;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.esri.android.mapbook.download.CredentialCryptographer;
import com.esri.android.mapbook.download.DownloadActivity;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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
public class CryptographerTest {

  @Inject CredentialCryptographer credentialCryptographer;

  @Rule public final ActivityTestRule<DownloadActivity> main =
      new ActivityTestRule<DownloadActivity>(DownloadActivity.class,
          true,     // initialTouchMode
          false);   // launchActivity. False so we can customize the intent per test method

  @Before
  public void setUp() {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    MapBookApplication app
        = (MapBookApplication) instrumentation.getTargetContext().getApplicationContext();

    DaggerTestComponent.builder().mockApplicationModule(new MockApplicationModule(app)).mockMapbookModule(new MockMapbookModule()).build().inject(this);
  }

  @Test
  public void testEncryptDecrypt(){
    // Launch activity
    main.launchActivity(new Intent());
    Assert.assertNotNull(credentialCryptographer);
    String filename = "test_cred";
    String testString = "This is a test string";
    String testAlias = "TEST_ALIAS";
    byte[] data = testString.getBytes();

    try {
      String path = credentialCryptographer.encrypt(data,filename, testAlias);
      String decryptedString = credentialCryptographer.decrypt(filename, testAlias);
      Assert.assertEquals(testString, decryptedString);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}

