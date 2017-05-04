/*
 * Copyright 2016 czy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ezy.boost.update;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker implements IUpdateChecker {

    final byte[] mPostData;

    public UpdateChecker() {
        mPostData = null;
    }
    public UpdateChecker(byte[] data) {
        mPostData = data;
    }

    @Override
    public void check(ICheckAgent agent, String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("Accept", "application/json");

            if (mPostData == null) {
                connection.setRequestMethod("GET");
                connection.connect();
            } else {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", Integer.toString(mPostData.length));
                connection.getOutputStream().write(mPostData);
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                agent.setInfo(UpdateUtil.readString(connection.getInputStream()));
            } else {
                agent.setError(new UpdateError(UpdateError.CHECK_HTTP_STATUS, "" + connection.getResponseCode()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            agent.setError(new UpdateError(UpdateError.CHECK_NETWORK_IO));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}