package com.mediclinic.onetoonechat.WelcomeSlide

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

import com.mediclinic.onetoonechat.Home.MainActivity
import com.mediclinic.onetoonechat.R

class IntroActivity : Activity() {

    lateinit var viewPager: ViewPager
    lateinit var viewPagerAdapter: ViewPagerAdapter
    lateinit var dotsLayout: LinearLayout
    lateinit var dots: Array<TextView?>
    lateinit var layouts: IntArray
    lateinit var btnSkip: Button
    lateinit var btnNext: Button

    /**
     * Add PageChangeLister for PageAdapter
     */
    internal var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                addBottomDots(position)

                /**
                 * changing the next button text 'NEXT' / 'GOT IT'
                 */
                if (position == layouts!!.size - 1) {
                    // last page. make button text to GOT IT
                    btnNext!!.text = getString(R.string.start)
                    btnSkip!!.visibility = View.GONE
                } else {
                    // still pages are left
                    btnNext!!.text = getString(R.string.next)
                    btnSkip!!.visibility = View.VISIBLE
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

            override fun onPageScrollStateChanged(arg0: Int) {}

        } //Add PageChangeLister for PageAdapter End

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //For Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.intro_activity)

        // saving in local cache through Shared Preferences
        val sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (sharedPreferences.getInt("INTRO", 0) == 1) {
            startActivity(Intent(this@IntroActivity, MainActivity::class.java))
            finish()
        }


        // initializations
        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)

        layouts = intArrayOf(R.layout.slide1, R.layout.slide2, R.layout.slide3, R.layout.slide4)

        // adding bottom dots
        addBottomDots(0)

        viewPagerAdapter = ViewPagerAdapter()
        viewPager!!.adapter = viewPagerAdapter
        viewPager!!.addOnPageChangeListener(viewPagerPageChangeListener)


    }

    //Click Listener for both buttons
    fun btnSkipClick(v: View) {
        launchHomeScreen()
    }

    fun btnNextClick(v: View) {
        // checking for last page
        /**
         * if last page home screen will be launched
         */
        val current = getItem(1)
        if (current < layouts!!.size) {
            /** move to next screen    */
            viewPager!!.currentItem = current
        } else {
            launchHomeScreen()
        }
    }


    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size)

        dotsLayout!!.removeAllViews()
        for (i in dots!!.indices) {
            dots[i] = TextView(this)
            dots!![i]!!.text = Html.fromHtml("&#8226;")
            dots!![i]!!.textSize = 35f
            dots!![i]!!.setTextColor(resources.getColor(R.color.dot_inactive))
            dotsLayout!!.addView(dots!![i])
        }

        if (dots!!.size > 0)
            dots!![currentPage]!!.setTextColor(resources.getColor(R.color.dot_active))
    }


    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    private fun launchHomeScreen() {

        /**
         * Save Intro Screen for First Time with shared
         */
        val sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor
        editor = sharedPreferences.edit()
        editor.putInt("INTRO", 1)
        editor.apply()


        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    /**
     * Create class extending PagerAdapter
     */
    inner class ViewPagerAdapter : PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view = layoutInflater!!.inflate(layouts!![position], container, false)
            container.addView(view)

            return view
        }

        override fun getCount(): Int {
            return layouts!!.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    } //Create class extending PagerAdapter End
}
