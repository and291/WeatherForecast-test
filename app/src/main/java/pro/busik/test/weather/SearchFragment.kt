package pro.busik.test.weather

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.*
import android.support.v7.widget.SearchView
import pro.busik.test.weather.utils.SafeLog


/**
 * A placeholder fragment containing a simple view.
 */
class SearchFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.fragment_search, menu)
        val searchMenuItem = menu!!.findItem(R.id.action_search)

        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                SafeLog.v("onQueryTextSubmit: ${query}")
                searchMenuItem.collapseActionView()
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                SafeLog.v("onQueryTextChange: ${query}")
                return true
            }
        })
    }
}
