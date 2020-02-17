package org.shadowrunrussia2020.android.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_billing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shadowrunrussia2020.android.R
import org.shadowrunrussia2020.android.common.utils.showErrorMessage


class BillingFragment : Fragment() {

    private lateinit var mModel: BillingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_billing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mModel = ViewModelProviders.of(activity!!).get(BillingViewModel::class.java)

        refreshData()

        swipeRefreshLayout.setOnRefreshListener { refreshData() }

        tabLayout.setupWithViewPager(viewPager)

        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getCount(): Int = 2
            override fun getItem(position: Int): Fragment {
                return if (position == 0) BillingOverviewFragment() else BillingHistoryFragment()
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) "Обзор" else "История"
            }
        }

        // Hack to un-break SwipeRefreshLayout and ViewPager interaction. Without that SwipeRefreshLayout
        // will trigger even with tiny bit of vertical movement during horizontal swipe.
        // Based on https://stackoverflow.com/a/35825488/11167405
        viewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                swipeRefreshLayout.isEnabled = state == ViewPager.SCROLL_STATE_IDLE;
            }
        })
    }

    private fun refreshData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) { mModel.refresh() }
            } catch (e: Exception) {
                showErrorMessage(requireContext(), "Ошибка. ${e.message}")
            }
            swipeRefreshLayout?.isRefreshing = false
        }
    }
}

