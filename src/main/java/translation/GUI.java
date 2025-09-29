package translation;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GUI extends JFrame {

    private final Translator translator;
    private final CountryCodeConverter countryConverter;
    private final LanguageCodeConverter languageConverter;

    private JComboBox<String> languageCombo;
    private JList<String> countryList;
    private JLabel resultLabel;

    private final Map<String, String> languageNameToCode = new HashMap<>();
    private final Map<String, String> countryNameToAlpha3 = new HashMap<>();

    public GUI(Translator translator,
               CountryCodeConverter countryConverter,
               LanguageCodeConverter languageConverter) {
        super("Country Name Translator");
        this.translator = translator;
        this.countryConverter = countryConverter;
        this.languageConverter = languageConverter;

        initData();
        initUI();
        initEvents();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initData() {
        for (String code : translator.getLanguageCodes()) {
            String langName = languageConverter.toName(code);
            if (langName != null) {
                languageNameToCode.put(langName, code);
            }
        }

        for (String alpha3 : translator.getCountryCodes()) {
            String countryName = countryConverter.fromCountryCode(alpha3.toUpperCase());
            if (countryName != null) {
                countryNameToAlpha3.put(countryName, alpha3);
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        // Top: language dropdown
        java.util.List<String> languageNames = new ArrayList<>(languageNameToCode.keySet());
        Collections.sort(languageNames);
        languageCombo = new JComboBox<>(languageNames.toArray(new String[0]));
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Language:"), BorderLayout.WEST);
        top.add(languageCombo, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Left: scrollable country list
        java.util.List<String> countryNames = new ArrayList<>(countryNameToAlpha3.keySet());
        Collections.sort(countryNames);
        countryList = new JList<>(countryNames.toArray(new String[0]));
        countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane leftScroll = new JScrollPane(countryList);
        leftScroll.setPreferredSize(new Dimension(250, 0));
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Country:"), BorderLayout.NORTH);
        left.add(leftScroll, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        // Center: result label
        resultLabel = new JLabel("—");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 28f));
        add(resultLabel, BorderLayout.CENTER);

        if (!countryNames.isEmpty()) {
            countryList.setSelectedIndex(0);
            updateTranslation();
        }
    }

    private void initEvents() {
        languageCombo.addActionListener(e -> updateTranslation());
        countryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTranslation();
            }
        });
    }

    private void updateTranslation() {
        String langName = (String) languageCombo.getSelectedItem();
        String countryName = countryList.getSelectedValue();
        if (langName == null || countryName == null) {
            resultLabel.setText("—");
            return;
        }

        String langCode = languageNameToCode.get(langName);
        String alpha3 = countryNameToAlpha3.get(countryName).toLowerCase();
        String translated = translator.translate(alpha3, langCode);

        if (translated == null || translated.isEmpty()) {
            resultLabel.setText("(no translation)");
        } else {
            resultLabel.setText(translated);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Translator translator = new JSONTranslator();
            CountryCodeConverter countryConverter = new CountryCodeConverter();
            LanguageCodeConverter languageConverter = new LanguageCodeConverter();
            new GUI(translator, countryConverter, languageConverter);
        });
    }
}