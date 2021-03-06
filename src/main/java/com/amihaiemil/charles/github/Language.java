/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.amihaiemil.charles.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Language that the agent speaks.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 * @todo #174:30m/DEV Implement and unit test Language class Russian.
 *  Follow class English and its unit tests as example. The class should
 *  use the commands_ru and responses_ru properties files.
 */
abstract class Language {
    
    private static final Logger LOG = LoggerFactory.getLogger(Language.class.getName());

    /**
     * Commands that the agent can understand, in a given language.
     */
    private Properties commandsPatterns = new Properties();
    
    /**
     * Responses that the agent can give, in a given language
     */
    private Properties responses = new Properties();
    
    Language(String commandsFileName, String responsesFileName) {
        try {
            commandsPatterns.load(
                this.getClass().getClassLoader().getResourceAsStream(commandsFileName)
            );
            responses.load(
                this.getClass().getClassLoader().getResourceAsStream(responsesFileName)
            );
        } catch (IOException e) {
            LOG.error("Exception when loading commands' patterns!", e);
            throw new IllegalStateException(e);
        }
    }
    
    CommandCategory categorize(Command command) throws IOException {
        Set<Object> keys = this.commandsPatterns.keySet();
        for(Object key : keys) {
            String keyString = (String) key;
            String regex = this.commandsPatterns.getProperty(keyString, "");
            if(!regex.isEmpty()) {
                String formattedRegex = String.format(regex, "@" + command.agentLogin());
                Pattern p = Pattern.compile(formattedRegex);
                String text = command.json().getString("body");
                Matcher m = p.matcher(text);
                if(m.matches()) {
                    return new CommandCategory(keyString.split("\\.")[0], this);
                }
            }
        }
        return new CommandCategory("unknown", this);
    }
    
    String response(String key) {
        return responses.getProperty(key);
    }
}
