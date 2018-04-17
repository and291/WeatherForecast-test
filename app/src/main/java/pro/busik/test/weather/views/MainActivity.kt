package pro.busik.test.weather.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import dagger.android.support.DaggerAppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import pro.busik.test.weather.R
import android.app.SearchManager
import android.content.Intent
import com.google.gson.Gson
import pro.busik.test.weather.model.City

class MainActivity : DaggerAppCompatActivity() {

    private val fragmentContainerId = R.id.fragmentContainer
    private val fm = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        handleIntent(intent!!)
    }

    private fun handleIntent(intent: Intent){
        var query: String = getString(R.string.default_city_name)
        var selectedCity: City? = null

        // Get the intent, verify the action and get the query
        when (intent.action){
            Intent.ACTION_SEARCH -> query = intent.getStringExtra(SearchManager.QUERY)
            getString(R.string.intent_search_by_city) -> {
                //can't pass parcelable because of MatrixCursor:
                //https://stackoverflow.com/questions/3034575/passing-binary-blob-through-a-content-provider/3034717#3034717
                val json = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY)
                selectedCity = Gson().fromJson<City>(json, City::class.java)
            }
        }
        
        fm.beginTransaction()
                .replace(fragmentContainerId, SearchFragment.newInstance(query))
                .commit()
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
