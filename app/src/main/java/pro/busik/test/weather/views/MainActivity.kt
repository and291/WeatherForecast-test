package pro.busik.test.weather.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.support.DaggerAppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import pro.busik.test.weather.R

class MainActivity : DaggerAppCompatActivity() {

    private val fragmentContainerId = R.id.fragmentContainer
    private val fm = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val fragment = fm.findFragmentById(fragmentContainerId)
        if(fragment == null){
            fm.beginTransaction()
                    .add(fragmentContainerId, SearchFragment.newInstance("Москва"))
                    .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_quit -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
