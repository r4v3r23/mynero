package net.mynero.wallet.fragment.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OnboardingViewModel extends ViewModel {
    private final MutableLiveData<Boolean> _showMoreOptions = new MutableLiveData<>(false);
    public LiveData<Boolean> showMoreOptions = _showMoreOptions;

    public void onMoreOptionsClicked() {
        boolean currentValue = showMoreOptions.getValue() != null ? showMoreOptions.getValue() : false;
        boolean newValue = !currentValue;
        _showMoreOptions.setValue(newValue);
    }
}