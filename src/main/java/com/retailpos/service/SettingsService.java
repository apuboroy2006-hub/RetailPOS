package com.retailpos.service;

import com.retailpos.model.Settings;
import com.retailpos.repository.SettingsRepository;

public class SettingsService {

    private final SettingsRepository settingsRepository;

    public SettingsService(
            SettingsRepository settingsRepository
    ) {
        this.settingsRepository =
                settingsRepository;
    }

    public Settings getSettings() {

        return settingsRepository.getSettings();
    }

    public void saveSettings(Settings settings) {

        if (settings == null) {
            throw new IllegalArgumentException(
                    "Settings cannot be null."
            );
        }

        if (settings.getStoreName() == null ||
                settings.getStoreName().isBlank()) {

            throw new IllegalArgumentException(
                    "Store name is required."
            );
        }

        if (settings.getCurrency() == null ||
                settings.getCurrency().isBlank()) {

            throw new IllegalArgumentException(
                    "Currency is required."
            );
        }

        settingsRepository.save(settings);
    }
}