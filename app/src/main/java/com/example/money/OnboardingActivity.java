package com.example.money;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.money.ui.auth.LoginActivity;
import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnSkip;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
            R.drawable.ic_onboarding_expenses,
            "Track Your Expenses",
            "Keep track of your daily expenses and income in one place"
        ));
        onboardingItems.add(new OnboardingItem(
            R.drawable.ic_onboarding_budget,
            "Set Budget Goals",
            "Create and manage your budget goals to save more money"
        ));
        onboardingItems.add(new OnboardingItem(
            R.drawable.ic_onboarding_analytics,
            "Analyze Your Spending",
            "Get insights into your spending habits with detailed analytics"
        ));

        adapter = new OnboardingAdapter(onboardingItems);
        viewPager.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == onboardingItems.size() - 1) {
                startLoginActivity();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        btnSkip.setOnClickListener(v -> startLoginActivity());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == onboardingItems.size() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void startLoginActivity() {
        startActivity(new Intent(OnboardingActivity.this, com.example.money.ui.auth.LoginActivity.class));
        finish();
    }
} 