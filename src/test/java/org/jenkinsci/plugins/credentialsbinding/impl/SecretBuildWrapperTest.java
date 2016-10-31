/*
 * The MIT License
 *
 * Copyright 2016 CloudBees inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.credentialsbinding.impl;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.tasks.Shell;
import hudson.util.Secret;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

public class SecretBuildWrapperTest {

    @Rule public JenkinsRule r = new JenkinsRule();

    @Issue("JENKINS-24805")
    @Test public void maskingFreeStyleSecrets() throws Exception {
        String firstCredentialsId = "creds_1";
        String firstPassword = "p4ss";
        StringCredentialsImpl firstCreds = new StringCredentialsImpl(CredentialsScope.GLOBAL, firstCredentialsId, "sample1", Secret.fromString(firstPassword));

        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), firstCreds);

        SecretBuildWrapper wrapper = new SecretBuildWrapper(Collections.singletonList(new StringBinding("PASS_1", firstCredentialsId)));

        FreeStyleProject f = r.createFreeStyleProject();

        f.setConcurrentBuild(true);
        f.getBuildersList().add(new Shell("echo $PASS_1"));
        f.getBuildWrappersList().add(wrapper);

        r.configRoundtrip((Item)f);

        FreeStyleBuild b = r.buildAndAssertSuccess(f);
        r.assertLogNotContains(firstPassword, b);
        r.assertLogContains("echo ****", b);
    }

}
