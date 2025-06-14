package com.example.newsapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.newsapp.Activity.DetailAuthorActivity;
import com.example.newsapp.Adapter.AuthorAdapter;
import com.example.newsapp.Interface.IClickItemUsersListener;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.UsersViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthorSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthorSearchFragment extends Fragment {
    private RecyclerView recyclerViewAuthor;
    private LinearLayout emptyView;

    private AuthorAdapter authorAdapter;
    private List<Users> authorList = new ArrayList<>();
    private UsersViewModel authorViewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AuthorSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthorSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthorSearchFragment newInstance(String param1, String param2) {
        AuthorSearchFragment fragment = new AuthorSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_author_search, container, false);
        authorViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(UsersViewModel.class);
        initViews(view);
        recyclerViewAuthor.setAdapter(authorAdapter);
        authorViewModel.getAllAuthors().observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                Log.d("AuthorSearchFragment", "Authors loaded: " + author.size());
                authorList.clear();
                authorList.addAll(author);
                authorAdapter.setAuthorList(authorList);
            } else {
                Log.d("AuthorSearchFragment", "No authors found");
            }
        });
        return view;
    }

    private void initViews(View view) {
        recyclerViewAuthor = view.findViewById(R.id.recyclerViewAuthor);
        emptyView = view.findViewById(R.id.emptyView);

        recyclerViewAuthor.setLayoutManager(new LinearLayoutManager(getContext()));
        authorAdapter = new AuthorAdapter(authorList, new IClickItemUsersListener() {
            @Override
            public void onItemClickedUsers(Users users) {
                Intent intent = new Intent(getContext(), DetailAuthorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("users", users);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            @Override
            public void onItemClickedFollow(Users users) {}
        });

    }

    public void performSearch(String query) {
        if (authorList == null || authorList.isEmpty()) {
            Log.d("AuthorSearchFragment", "Author list is empty or null");
            return;
        }

        List<Users> filteredList = new ArrayList<>();
        for (Users users : authorList) {
            if (users.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(users);
            }
        }

        if (filteredList.isEmpty()) {
            Log.d("AuthorSearchFragment", "No authors found for query: " + query);
            recyclerViewAuthor.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            Log.d("AuthorSearchFragment", "Found " + filteredList.size() + " authors for query: " + query);
            recyclerViewAuthor.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        authorAdapter.setAuthorList(filteredList);
    }
}