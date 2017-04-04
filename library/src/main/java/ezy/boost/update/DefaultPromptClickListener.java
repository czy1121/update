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

import android.content.DialogInterface;

public class DefaultPromptClickListener implements DialogInterface.OnClickListener {
    private final IUpdateAgent mAgent;
    private final boolean mIsAutoDismiss;

    public DefaultPromptClickListener(IUpdateAgent agent, boolean isAutoDismiss) {
        mAgent = agent;
        mIsAutoDismiss = isAutoDismiss;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            mAgent.update();
            break;
        case DialogInterface.BUTTON_NEUTRAL:
            mAgent.ignore();
            break;
        case DialogInterface.BUTTON_NEGATIVE:
            // not now
            break;
        }
        if (mIsAutoDismiss) {
            dialog.dismiss();
        }
    }
}