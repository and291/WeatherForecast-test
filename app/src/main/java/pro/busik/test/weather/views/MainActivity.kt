package pro.busik.test.weather.views

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import dagger.android.support.DaggerAppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import pro.busik.test.weather.R
import android.app.SearchManager
import android.content.Intent
import com.google.gson.Gson
import pro.busik.test.weather.model.data.City
import pro.busik.test.weather.model.data.SearchQuery

class MainActivity : DaggerAppCompatActivity() {

    private val fragmentContainerId = R.id.fragmentContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //create fragment if there is no one
        if(supportFragmentManager.findFragmentById(fragmentContainerId) == null){
            replaceFragment(getSearchQuery())
        }
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)

        //new search initiated: update intent and replace fragment
        intent = newIntent
        replaceFragment(getSearchQuery())
    }

    private fun getSearchQuery() : SearchQuery {
        // SearchQuery depends on intent action
        return when (intent.action){
            //search by text query
            Intent.ACTION_SEARCH -> SearchQuery(intent.getStringExtra(SearchManager.QUERY), null)

            //search by selected suggestion
            //can't pass parcelable because of MatrixCursor:
            //https://stackoverflow.com/questions/3034575/passing-binary-blob-through-a-content-provider/3034717#3034717
            getString(R.string.intent_search_by_city) -> {
                val json = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY)
                val selectedCity = Gson().fromJson<City>(json, City::class.java)
                SearchQuery(selectedCity.name, selectedCity)
            }

            //search by default city name
            else -> SearchQuery(getString(R.string.default_city_name), null)
        }
    }

    private fun replaceFragment(searchQuery: SearchQuery){
        supportFragmentManager.beginTransaction()
                .replace(fragmentContainerId, SearchFragment.newInstance(searchQuery))
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
