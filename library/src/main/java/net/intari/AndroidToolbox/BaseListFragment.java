package net.intari.AndroidToolbox;

import android.support.v4.app.ListFragment;

import net.intari.CustomLogger.CustomLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;



/**
 * Created by Dmitriy Kazimirov, e-mail dmitriy.kazimirov@viorsan.com on 13.03.15.
 * ListFragment-based version of BaseFragment
 */
public class BaseListFragment extends ListFragment {

        public static final String TAG = BaseListFragment.class.getName();

        // UI Runnables
        private final List<Runnable> mUiRunnables = new LinkedList<Runnable>();
        private boolean mIsPaused = false;
        // ============

        @Override
        public void onResume()
        {
            super.onResume();
            mIsPaused = false;
            runQueuedUiRunnables();
        }

        @Override
        public void onPause()
        {
            mIsPaused = true;
            super.onPause();
        }

        /**
         * Is the fragment paused?
         *
         * @return
         */
        public boolean isPaused()
        {
            return mIsPaused;
        }

        /**
         * Add a runnable task that can only be run during the activity being alive, things like dismissing dialogs when a background
         * task completes when the user is away from the activity.
         *
         * @param runnable runnable to run during the ui being alive.
         */
        protected void postUiRunnable(final Runnable runnable)
        {
            //CustomLog.v(TAG, "UiRunnables = " + runnable);
            if (null == runnable){
                return;
            }
            if (!mIsPaused && BaseActivity.isUiThread())
            {
                runnable.run();
            }
            else if (!mIsPaused && !BaseActivity.isUiThread() && getActivity() != null)
            {
                getActivity().runOnUiThread(runnable);
            }
            else
            {
                mUiRunnables.add(runnable);
            }
        }

        /**
         * Will run any pending UiRunnables on resuming the activity
         */
        private void runQueuedUiRunnables()
        {
            if (mIsPaused) return;
            if (mUiRunnables.isEmpty()) return;

            //CustomLog.d(TAG,"UiRunnables Running");
            final Iterator<Runnable> it = mUiRunnables.iterator();
            Runnable run;
            while (it.hasNext())
            {
                run = it.next();
                run.run();
                it.remove();
            }
        }

}
