package io.codiga.plugins.jetbrains.ui;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTextBorder;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTextFieldUI;
import com.intellij.ide.util.gotoByName.ContributorsBasedGotoByModel;
import com.intellij.ide.util.gotoByName.ModelDiff;
import com.intellij.ide.util.treeView.TreeAnchorizer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.AbstractPopup;
import com.intellij.ui.popup.PopupPositionManager;
import com.intellij.ui.popup.PopupUpdateProcessor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.Alarm;
import com.intellij.util.ArrayUtil;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.Matcher;
import com.intellij.util.text.MatcherHolder;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class SearchPopup implements Disposable {
    public static final String TEMPORARILY_FOCUSABLE_COMPONENT_KEY = "Codiga.SearchPopup.TemporarilyFocusableComponent";

    public static final Key<SearchPopup> CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY = new Key<>("CodigaSearchPopup");
    public static final Key<String> CURRENT_SEARCH_PATTERN = new Key<>("CodigaSearchPattern");

    private static final Logger LOG = Logger.getInstance(SearchPopup.class);

    @Nullable private final Project project;
    @NotNull private final Model model;
    @NotNull private final SearchItemProvider itemProvider;
    final JPanelProvider textFieldPanel = new JPanelProvider();
    final SearchTextField textField = new SearchTextField();
    private Boolean initialized = false;

    protected final Alarm alarm = new Alarm();
    private final Alarm updateListAlarm = new Alarm();

    private final ProgressIndicator progressIndicator = new ProgressIndicatorBase();

    Component previouslyFocusedComponent;
    JBPopup textPopup;
    boolean disposedFlag = false;

    protected Callback actionListener;
    final int rebuildDelay;

    JScrollPane listScrollPane;
    private final SmartPointerListModel<Object> listModel = new SmartPointerListModel<>();
    protected final JList<Object> list = new JBList<>(listModel);

    private JPanel loader;

    private JPanel notFoundPanel;

    public SearchPopup(@Nullable final Project project, @NotNull final Model model, @NotNull final SearchItemProvider itemProvider) {
        this.project = project;
        this.model = model;
        this.itemProvider = itemProvider;
        rebuildDelay = Registry.intValue("ide.goto.rebuild.delay");
    }

    public void invoke(@NotNull Callback callback) {
        previouslyFocusedComponent = WindowManagerEx.getInstanceEx().getFocusedComponent(project);
        actionListener = callback;

        initUI();

        showTextFieldPanel();

        initialized = true;

        rebuildList();
    }

    private void initUI() {
        textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.Y_AXIS));

        textFieldPanel.add(textField);
        Font editorFont = EditorUtil.getEditorFont();
        textField.setFont(editorFont);
        textField.putClientProperty("caretWidth", JBUIScale.scale(EditorUtil.getDefaultCaretWidth()));

        final Set<KeyStroke> upShortcuts = getShortcuts(IdeActions.ACTION_EDITOR_MOVE_CARET_UP);
        final Set<KeyStroke> downShortcuts = getShortcuts(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN);

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(@NotNull KeyEvent e) {
                /**
                 * If key is enter, we add the data and exit.
                 */
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Object o = getChosenElement();
                    if (o != null) {
                        actionListener.elementChosen(o);
                    }

                    close(true);
                }
                if (!listScrollPane.isVisible()) {
                    return;
                }
                final int keyCode;

                // Add support for user-defined 'caret up/down' shortcuts.
                KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                if (upShortcuts.contains(stroke)) {
                    keyCode = KeyEvent.VK_UP;
                } else if (downShortcuts.contains(stroke)) {
                    keyCode = KeyEvent.VK_DOWN;
                } else {
                    keyCode = e.getKeyCode();
                }

                switch (keyCode) {
                    case KeyEvent.VK_DOWN:
                        ScrollingUtil.moveDown(list, e.getModifiersEx());
                        break;
                    case KeyEvent.VK_UP:
                        ScrollingUtil.moveUp(list, e.getModifiersEx());
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        ScrollingUtil.movePageUp(list);
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        ScrollingUtil.movePageDown(list);
                        break;
                    case KeyEvent.VK_TAB:
                        close(true);
                        break;
                    default:
                        // do nothing
                }
            }
        });

        notFoundPanel = new JPanel();
        notFoundPanel.setLayout(new BoxLayout(notFoundPanel, BoxLayout.X_AXIS));

        JLabel notFoundLabel = new JLabel(model.getNotFoundText());
        notFoundLabel.setForeground(JBColor.GRAY);
        notFoundPanel.add(notFoundLabel);
        textFieldPanel.add(notFoundPanel);
        notFoundPanel.setVisible(false);

        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent e, int clickCount) {
                if (!textField.hasFocus()) {
                    IdeFocusManager.getInstance(project).requestFocus(textField, true);
                }

                if (clickCount == 2) {
                    int selectedIndex = list.getSelectedIndex();
                    Rectangle selectedCellBounds = list.getCellBounds(selectedIndex, selectedIndex);

                    if (selectedCellBounds != null && selectedCellBounds.contains(e.getPoint())) { // Otherwise it was reselected in the selection listener
                        Object selectedValue = list.getSelectedValue();

                        /**
                         * If something is selected, we invoke the callback to insert the code
                         * into the editor.
                         */
                        if (selectedValue != null) {
                           actionListener.elementChosen(selectedValue);
                        }
                        doClose();
                    }
                    return true;
                }
                return false;
            }
        }.installOn(list);

        ListCellRenderer modelRenderer = model.getListCellRenderer();
        //noinspection unchecked
        list.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> AstLoadingFilter.disallowTreeLoading(
                () -> modelRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        ));
        list.setVisibleRowCount(16);
        list.setFont(editorFont);

        list.addListSelectionListener(__ -> {
            if (checkDisposed()) {
                return;
            }

            //chosenElementMightChange();
            updateDocumentation();
        });

        listScrollPane = ScrollPaneFactory.createScrollPane(list, true);

        JPanel listPanel = new JPanel(new CardLayout());
        listPanel.add(listScrollPane);
        listPanel.setBorder(JBUI.Borders.emptyTop(5));
        textFieldPanel.add(listPanel, BorderLayout.NORTH);

        loader = new JPanel();
        loader.setLayout(new BoxLayout(loader, BoxLayout.X_AXIS));
        loader.add(new AsyncProcessIcon("loading..."));
        loader.add(new JLabel(" loading..."));
        textFieldPanel.add(loader);

        textFieldPanel.setBorder(JBUI.Borders.empty(5));
    }

    private void showLoader() {
        loader.setVisible(true);
    }

    private void hideLoader() {
        loader.setVisible(false);
    }

    @NotNull
    protected List<Object> getChosenElements() {
        return ContainerUtil.filter(list.getSelectedValuesList(), o -> o != null);
    }

    @Nullable
    public Object getChosenElement() {
        final List<Object> elements = getChosenElements();
        return elements.size() == 1 ? elements.get(0) : null;
    }

    private void updateDocumentation() {
        final JBPopup hint = textFieldPanel.getHint();
        final Object element = getChosenElement();
        if (hint != null) {
            if (element instanceof PsiElement) {
                textFieldPanel.updateHint((PsiElement)element);
            }
            else if (element instanceof DataProvider) {
                final Object o = ((DataProvider)element).getData(CommonDataKeys.PSI_ELEMENT.getName());
                if (o instanceof PsiElement) {
                    textFieldPanel.updateHint((PsiElement)o);
                }
            }
        }
    }

    private void showTextFieldPanel() {
        final JLayeredPane layeredPane = getLayeredPane();
        final Dimension preferredTextFieldPanelSize = textFieldPanel.getPreferredSize();
        final int x = (layeredPane.getWidth() - preferredTextFieldPanelSize.width) / 2;
        final int paneHeight = layeredPane.getHeight();
        final int y = paneHeight / 3 - preferredTextFieldPanelSize.height / 2;

        ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(textFieldPanel, textField);
        builder.setLocateWithinScreenBounds(false);

        builder.setCancelOnWindowDeactivation(true)
                .setMovable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setModalContext(false)
                .setCancelOnClickOutside(true)
                .setTitle(model.getTitleText());

        builder.setKeyEventHandler(event -> {
            if (textPopup == null || !AbstractPopup.isCloseRequest(event) || !textPopup.isCancelKeyEnabled()) {
                return false;
            }

            IdeFocusManager focusManager = IdeFocusManager.getInstance(project);
            if (isDescendingFromTemporarilyFocusableToolWindow(focusManager.getFocusOwner())) {
                focusManager.requestFocus(textField, true);
                return false;
            } else {
                textPopup.cancel(event);
                return true;
            }
        }).setCancelCallback(() -> {
            textPopup = null;
            close(false);
            return Boolean.TRUE;
        });

        textPopup = builder.createPopup();

        Point point = new Point(x, y);
        SwingUtilities.convertPointToScreen(point, layeredPane);
        Rectangle bounds = new Rectangle(point, new Dimension(preferredTextFieldPanelSize.width + 20, preferredTextFieldPanelSize.height));
        textPopup = builder.createPopup();
        textPopup.setSize(bounds.getSize());
        textPopup.setLocation(bounds.getLocation());

        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
        textPopup.show(layeredPane);

        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent e, int clickCount) {
                if (!textField.hasFocus()) {
                    IdeFocusManager.getInstance(project).requestFocus(textField, true);
                }

                if (clickCount == 2) {
                    int selectedIndex = list.getSelectedIndex();
                    Rectangle selectedCellBounds = list.getCellBounds(selectedIndex, selectedIndex);

                    if (selectedCellBounds != null && selectedCellBounds.contains(e.getPoint())) { // Otherwise it was reselected in the selection listener
                        doClose();
                    }
                    return true;
                }

                return false;
            }
        }.installOn(list);

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                rebuildList(200);
            }
        });
    }

    private void rebuildList() {
        rebuildList(rebuildDelay);
    }

    private void rebuildList(int delay) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (!initialized) {
            return;
        }

        alarm.cancelAllRequests();

        if (delay > 0) {
            alarm.addRequest(() -> rebuildList(0), delay, ModalityState.stateForComponent(textField));
            return;
        }

        final String text = getTrimmedText();
        final String pattern = patternToLowerCase(text);
        final Matcher matcher = buildPatternMatcher(pattern);
        MatcherHolder.associateMatcher(list, matcher);

        runBackgroundProcess();
    }

    private void runBackgroundProcess() {
        if (project == null || DumbService.isDumbAware(model)) return;

        DumbService.getInstance(project).runReadActionInSmartMode(this::performInReadAction);
    }

    private void performInReadAction() {
        if (isProjectDisposed()) return;

        final String text = getTrimmedText();
        final String pattern = patternToLowerCase(text);
        final Matcher matcher = buildPatternMatcher(pattern);
        MatcherHolder.associateMatcher(list, matcher);

        showLoader();

        Set<Object> elements = Collections.synchronizedSet(new LinkedHashSet<>());
        scheduleIncrementalListUpdate(elements);
        addElementsByPattern(pattern, elements, progressIndicator);

        if (elements.isEmpty()) {
            list.setVisible(false);
            notFoundPanel.setVisible(true);
        } else {
            notFoundPanel.setVisible(false);
            list.setVisible(true);
        }
    }

    private void addElementsByPattern(
        @NotNull String pattern,
        @NotNull final Set<Object> elements,
        @NotNull final ProgressIndicator indicator
    ) {
        long start = System.currentTimeMillis();
        itemProvider.filterElements(
                model,
                pattern,
                indicator,
                o -> {
                    if (indicator.isCanceled()) return false;
                    if (o == null) {
                        LOG.error("Null returned from " + itemProvider + " with " + model + " in " + this);
                        return true;
                    }
                    elements.add(o);
                    return true;
                }
        );
        if (ContributorsBasedGotoByModel.LOG.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            ContributorsBasedGotoByModel.LOG.debug("addElementsByPattern("+pattern+"): "+(end-start)+"ms; "+elements.size()+" elements");
        }
    }

    private void scheduleIncrementalListUpdate(@NotNull Set<Object> elements) {
        if (ApplicationManager.getApplication().isUnitTestMode()) return;
        updateListAlarm.addRequest(() -> {
            setElementsToList(new ArrayList<>(elements));
            list.repaint();

            hideLoader();

            list.setSelectedIndex(0);

        }, 200);
    }

    private void setElementsToList(@NotNull Collection<?> elements) {
        if (checkDisposed()) return;
        if (elements.isEmpty()) {
            listModel.removeAll();
            textField.setForeground(JBColor.red);
            return;
        }

        Object[] oldElements = listModel.getItems().toArray();
        Object[] newElements = elements.toArray();
        if (ArrayUtil.contains(null, newElements)) {
            LOG.error("Null after filtering elements by " + this);
        }
        List<ModelDiff.Cmd> commands = ModelDiff.createDiffCmds(listModel, oldElements, newElements);

        textField.setForeground(UIUtil.getTextFieldForeground());
        if (commands != null && !commands.isEmpty()) {
            appendToModel(commands);
        }
    }

    private void appendToModel(@NotNull List<? extends ModelDiff.Cmd> commands) {
        for (ModelDiff.Cmd command : commands) {
            command.apply();
        }

        if (listModel.isNotEmpty()) {
            list.setSelectedIndices(new int[]{1});
        }
    }

    private boolean isProjectDisposed() {
        return project != null && project.isDisposed();
    }

    @NotNull
    @NlsSafe
    public String getTrimmedText() {
        return StringUtil.trimLeading(StringUtil.notNullize(textField.getText()));
    }

    @NotNull
    private static Matcher buildPatternMatcher(@NotNull String pattern) {
        return NameUtil.buildMatcher(pattern, NameUtil.MatchingCaseSensitivity.NONE);
    }

    private void close(boolean isOk) {
        if (checkDisposed()){
            return;
        }

        Disposer.dispose(this);
        setDisposed(true);
        alarm.cancelAllRequests();

        cleanupUI(isOk);
        if (ApplicationManager.getApplication().isUnitTestMode()) return;
        if (actionListener != null) {
            actionListener.onClose();
        }
    }

    protected void doClose() {
        if (checkDisposed()) return;

        close(true);

        listModel.removeAll();
    }

    @NotNull
    @NlsSafe
    private static String patternToLowerCase(@NotNull @NlsSafe String pattern) {
        return StringUtil.toLowerCase(pattern);
    }

    private boolean isDescendingFromTemporarilyFocusableToolWindow(@Nullable Component component) {
        if (component == null || project == null || project.isDisposed()) return false;

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        String activeToolWindowId = toolWindowManager.getActiveToolWindowId();
        ToolWindow toolWindow = activeToolWindowId == null ? null : toolWindowManager.getToolWindow(activeToolWindowId);
        JComponent toolWindowComponent = toolWindow == null ? null : toolWindow.getComponent();
        return toolWindowComponent != null &&
                toolWindowComponent.getClientProperty(TEMPORARILY_FOCUSABLE_COMPONENT_KEY) != null &&
                SwingUtilities.isDescendingFrom(component, toolWindowComponent);
    }

    public boolean checkDisposed() {
        return disposedFlag;
    }

    public void setDisposed(boolean disposedFlag) {
        this.disposedFlag = disposedFlag;
    }

    private void cleanupUI(boolean ok) {
        if (textPopup != null) {
            if (ok) {
                textPopup.closeOk(null);
            }
            else {
                textPopup.cancel();
            }
            textPopup = null;
        }
    }

    private JLayeredPane getLayeredPane() {
        JLayeredPane layeredPane;
        final Window window = WindowManager.getInstance().suggestParentWindow(project);

        if (window instanceof JFrame) {
            layeredPane = ((JFrame)window).getLayeredPane();
        } else if (window instanceof JDialog) {
            layeredPane = ((JDialog)window).getLayeredPane();
        } else if (window instanceof JWindow) {
            layeredPane = ((JWindow)window).getLayeredPane();
        } else {
            throw new IllegalStateException("cannot find parent window: project=" + project +
                    (project == null ? "" : "; open=" + project.isOpen()) + "; window=" + window);
        }
        return layeredPane;
    }

    @Override
    public void dispose() {
        if (project != null) {
            project.putUserData(CURRENT_SEARCH_PATTERN, null);
            project.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, null);
        }
    }

    @NotNull
    private static Set<KeyStroke> getShortcuts(@NotNull String actionId) {
        Set<KeyStroke> result = new HashSet<>();
        for (Shortcut shortcut : KeymapUtil.getActiveKeymapShortcuts(actionId).getShortcuts()) {
            if (shortcut instanceof KeyboardShortcut) {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut)shortcut;
                result.add(keyboardShortcut.getFirstKeyStroke());
            }
        }
        return result;
    }

    private static class JPanelProvider extends JPanel {
        private JBPopup myHint;

        JPanelProvider() {
            super();
        }

        public void hideHint() {
            if (myHint != null) {
                myHint.cancel();
            }
        }

        @Nullable
        public JBPopup getHint() {
            return myHint;
        }

        void updateHint(PsiElement element) {
            if (myHint == null || !myHint.isVisible()) return;
            final PopupUpdateProcessor updateProcessor = myHint.getUserData(PopupUpdateProcessor.class);
            if (updateProcessor != null) {
                updateProcessor.updatePopup(element);
            }
        }

        void repositionHint() {
            if (myHint == null || !myHint.isVisible()) return;
            PopupPositionManager.positionPopupInBestPosition(myHint, null, null);
        }
    }

    private static class SearchTextField extends JTextField {
        SearchTextField() {
            super(40);
            if (!UIUtil.isUnderDefaultMacTheme() && !UIUtil.isUnderWin10LookAndFeel()) {
                if (!(getUI() instanceof DarculaTextFieldUI)) {
                    setUI(DarculaTextFieldUI.createUI(this));
                }
                setBorder(new DarculaTextBorder());
            }

            enableEvents(AWTEvent.KEY_EVENT_MASK);
            setFocusTraversalKeysEnabled(false);
            putClientProperty("JTextField.variant", "search");
            setMaximumSize(new Dimension(500, 40));
        }
    }

    public abstract static class Callback {
        public abstract void elementChosen(Object var1);

        public abstract void onClose();
    }

    public interface Model {
        @NlsContexts.PopupTitle @NotNull String getTitleText();

        default @NlsContexts.Label @NotNull String getNotFoundText() {
            return IdeBundle.message("label.choosebyname.no.matches.found");
        }

        @NotNull ListCellRenderer getListCellRenderer();
    }

    static class SmartPointerListModel<T> extends AbstractListModel<T> implements ModelDiff.Model<T> {
        private final CollectionListModel<Object> myDelegate = new CollectionListModel<>();

        SmartPointerListModel() {
            myDelegate.addListDataListener(new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent e) {
                    fireIntervalAdded(e.getSource(), e.getIndex0(), e.getIndex1());
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                    fireIntervalRemoved(e.getSource(), e.getIndex0(), e.getIndex1());
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    fireContentsChanged(e.getSource(), e.getIndex0(), e.getIndex1());
                }
            });
        }

        @Override
        public int getSize() {
            return myDelegate.getSize();
        }

        @Override
        public T getElementAt(int index) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            return unwrap(myDelegate.getElementAt(index));
        }

        private Object wrap(T element) {
            return TreeAnchorizer.getService().createAnchor(element);
        }

        private T unwrap(Object at) {
            //noinspection unchecked
            return (T)TreeAnchorizer.getService().retrieveElement(at);
        }

        @Override
        public void addToModel(int idx, T element) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            myDelegate.add(Math.min(idx, getSize()), wrap(element));
        }

        @Override
        public void addAllToModel(int index, java.util.List<? extends T> elements) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            myDelegate.addAll(Math.min(index, getSize()), ContainerUtil.map(elements, this::wrap));
        }

        @Override
        public void removeRangeFromModel(int start, int end) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            if (start < getSize() && isNotEmpty()) {
                myDelegate.removeRange(start, Math.min(end, getSize() - 1));
            }
        }

        boolean isNotEmpty() {
            return getSize() != 0;
        }

        void removeAll() {
            myDelegate.removeAll();
        }

        boolean contains(T elem) {
            return getItems().contains(elem);
        }

        List<T> getItems() {
            return ContainerUtil.map(myDelegate.getItems(), this::unwrap);
        }
    }

    public interface SearchItemProvider {
        void filterElements(@NotNull Model model, @NotNull String names, @NotNull ProgressIndicator progressIndicator, @NotNull Processor<Object> processor);
    }

}
