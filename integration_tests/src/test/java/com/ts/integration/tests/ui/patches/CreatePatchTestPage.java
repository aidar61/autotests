package com.ts.integration.tests.ui.patches;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.ui.pages.portlets.PatchPage;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static com.codeborne.selenide.Selenide.open;


public class CreatePatchTestPage extends BaseUiTest {
    LoginPage loginPage;
    PatchPage patchPage;

    String url = "http://tsdev7.dev.colvir.ru/TrackStudio/app/portlet/patch?branch=AZPST";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        patchPage = trackStudioPages.getPatchPage();
    }

    @Test
    public void createByAdmin(){
        String user = "root";

        open(url);
        loginPage.loginNoToken(user);
        patchPage.setConfiguration()
                 .openTaskForm()
                 .udfPatchOwnerIsPresent();

    }
}
