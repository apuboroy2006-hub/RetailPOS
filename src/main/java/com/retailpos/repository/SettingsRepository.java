package com.retailpos.repository;

import com.retailpos.model.Settings;

public interface SettingsRepository {

    Settings getSettings();

    void save(Settings settings);
}