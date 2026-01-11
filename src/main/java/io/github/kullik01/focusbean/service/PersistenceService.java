/**
 * BSD 3-Clause License
 *
 * Copyright (c) 2026, Hannah Kullik
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.kullik01.focusbean.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.kullik01.focusbean.model.SessionHistory;
import io.github.kullik01.focusbean.model.TimerSession;
import io.github.kullik01.focusbean.model.UserSettings;
import io.github.kullik01.focusbean.util.AppConstants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles persistence of application data to JSON files.
 *
 * <p>
 * This service manages reading and writing of session history and user settings
 * to the application data directory ({@code %APPDATA%/FocusBean/}).
 * </p>
 *
 * <p>
 * The service uses Gson for JSON serialization with custom adapters for
 * Java 8+ date/time types.
 * </p>
 *
 * <p>
 * Thread safety: This class is not thread-safe. External synchronization
 * is required if accessed from multiple threads.
 * </p>
 */
public final class PersistenceService {

    private static final Logger LOGGER = Logger.getLogger(PersistenceService.class.getName());

    private final Path dataDirectory;
    private final Path dataFile;
    private final Gson gson;

    /**
     * Creates a new PersistenceService using the default data directory.
     *
     * <p>
     * The default directory is {@code %APPDATA%/FocusBean/} on Windows.
     * </p>
     */
    public PersistenceService() {
        this(resolveDefaultDataDirectory());
    }

    /**
     * Creates a new PersistenceService with a custom data directory.
     *
     * <p>
     * This constructor is primarily intended for testing purposes.
     * </p>
     *
     * @param dataDirectory the directory to store data files
     * @throws NullPointerException if dataDirectory is null
     */
    public PersistenceService(Path dataDirectory) {
        Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");

        this.dataDirectory = dataDirectory;
        this.dataFile = dataDirectory.resolve(AppConstants.SESSION_HISTORY_FILENAME);
        this.gson = createGson();

        LOGGER.log(Level.FINE, "PersistenceService initialized with directory: {0}", dataDirectory);
    }

    /**
     * Saves the application data to disk.
     *
     * <p>
     * This will create the data directory if it does not exist.
     * </p>
     *
     * @param settings the user settings to save
     * @param history  the session history to save
     * @throws NullPointerException if settings or history is null
     */
    public void save(UserSettings settings, SessionHistory history) {
        Objects.requireNonNull(settings, "settings must not be null");
        Objects.requireNonNull(history, "history must not be null");

        try {
            ensureDataDirectoryExists();

            ApplicationData data = new ApplicationData(
                    1, // schema version
                    settings,
                    history.getSessions());

            try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }

            LOGGER.log(Level.INFO, "Saved {0} sessions to {1}",
                    new Object[] { history.size(), dataFile });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save data to " + dataFile, e);
        }
    }

    /**
     * Loads the application data from disk.
     *
     * <p>
     * If the data file does not exist or cannot be read, default values
     * are returned.
     * </p>
     *
     * @return the loaded data, or default values if no data exists
     */
    public LoadedData load() {
        if (!Files.exists(dataFile)) {
            LOGGER.info("No data file found, using defaults");
            return new LoadedData(new UserSettings(), new SessionHistory());
        }

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            ApplicationData data = gson.fromJson(reader, ApplicationData.class);

            if (data == null) {
                LOGGER.warning("Data file was empty, using defaults");
                return new LoadedData(new UserSettings(), new SessionHistory());
            }

            UserSettings settings = data.settings != null ? data.settings : new UserSettings();
            SessionHistory history = data.sessions != null
                    ? new SessionHistory(data.sessions)
                    : new SessionHistory();

            LOGGER.log(Level.INFO, "Loaded {0} sessions from {1}",
                    new Object[] { history.size(), dataFile });

            return new LoadedData(settings, history);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load data from " + dataFile, e);
            return new LoadedData(new UserSettings(), new SessionHistory());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse data from " + dataFile, e);
            return new LoadedData(new UserSettings(), new SessionHistory());
        }
    }

    /**
     * Checks if a data file exists.
     *
     * @return {@code true} if the data file exists and is readable
     */
    public boolean hasExistingData() {
        return Files.exists(dataFile) && Files.isReadable(dataFile);
    }

    /**
     * Returns the path to the data file.
     *
     * @return the data file path
     */
    public Path getDataFile() {
        return dataFile;
    }

    /**
     * Returns the path to the data directory.
     *
     * @return the data directory path
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Resolves the default data directory using the APPDATA environment variable.
     *
     * @return the resolved data directory path
     */
    private static Path resolveDefaultDataDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            // Fallback to user home if APPDATA is not set
            appData = System.getProperty("user.home");
            LOGGER.warning("APPDATA not set, falling back to user.home: " + appData);
        }
        return Paths.get(appData, AppConstants.APP_DATA_DIR_NAME);
    }

    /**
     * Creates and configures the Gson instance with custom type adapters.
     *
     * @return the configured Gson instance
     */
    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Ensures the data directory exists, creating it if necessary.
     *
     * @throws IOException if the directory cannot be created
     */
    private void ensureDataDirectoryExists() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
            LOGGER.log(Level.INFO, "Created data directory: {0}", dataDirectory);
        }
    }

    /**
     * Internal data class for JSON serialization.
     */
    private record ApplicationData(
            int version,
            UserSettings settings,
            List<TimerSession> sessions) {
    }

    /**
     * Container for loaded data.
     *
     * @param settings the loaded user settings
     * @param history  the loaded session history
     */
    public record LoadedData(UserSettings settings, SessionHistory history) {
        /**
         * Creates a LoadedData container.
         *
         * @throws NullPointerException if settings or history is null
         */
        public LoadedData {
            Objects.requireNonNull(settings, "settings must not be null");
            Objects.requireNonNull(history, "history must not be null");
        }
    }

    /**
     * Gson TypeAdapter for LocalDateTime serialization.
     *
     * <p>
     * Serializes LocalDateTime as ISO-8601 strings.
     * </p>
     */
    private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(FORMATTER));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), FORMATTER);
        }
    }
}
