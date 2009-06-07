/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.winrun4j;

public class FileAssociations
{
    public static FileAssociation load(String extension) {
        RegistryKey k = RegistryKey.HKEY_CLASSES_ROOT.getSubKey(extension);
        FileAssociation fa = new FileAssociation(extension);
        fa.setName(k.getString(null));
        fa.setContentType(k.getString("Content Type"));
        fa.setPerceivedType(k.getString("PerceivedType"));
        RegistryKey ok = k.getSubKey("OpenWithList");
        String[] owk = ok.getSubKeyNames();
        if (owk != null) {
            for (int i = 0; i < owk.length; i++) {
                fa.addOpenWith(owk[i]);
            }
        }
        if (fa.getName() == null) {
            return fa;
        }

        // Load FileType section
        RegistryKey n = new RegistryKey(RegistryKey.HKEY_CLASSES_ROOT, fa.getName());
        fa.setDescription(n.getString(null));
        RegistryKey di = new RegistryKey(n, "DefaultIcon");
        fa.setIcon(di.getExpandedString(null));
        RegistryKey sk = new RegistryKey(n, "shell");
        String[] skn = sk.getSubKeyNames();
        for (int i = 0; i < skn.length; i++) {
            FileVerb fv = new FileVerb(skn[i]);
            RegistryKey fvk = sk.getSubKey(skn[i]);
            fv.setLabel(fvk.getString(null));
            RegistryKey ck = fvk.getSubKey("command");
            fv.setCommand(ck.getString(null));
            RegistryKey dk = fvk.getSubKey("ddeexec");
            fv.setDDECommand(dk.getString(null));
            RegistryKey adk = dk.getSubKey("Application");
            fv.setDDEApplication(adk.getString(null));
            RegistryKey tdk = dk.getSubKey("Topic");
            fv.setDDETopic(tdk.getString(null));
            fa.put(fv);
        }

        return fa;
    }
}
