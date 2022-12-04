/**
 * Provides an API to manipulate a limited XML DOM.
 *  
 * <p>
 * The classes provides such a API useful for serializing objects into XML format.
 * <p>
 * The XML standard used follows this example:
 *
 * <pre>
 *  &lt;tagname&gt;
 *      &lt;tagname2&gt; a content &lt;/tagname2&gt;
 *      &lt;tagname3&gt; 42 &lt;/tagname3&gt;
 *  &lt;/tagname&gt;
 * </pre>
 * 
 * - Only one record (the root record) in a given input (String, file, stream, ...).<br>
 * - The root contain nested records.<br>
 * - A record can carry one text content XOR one to many records.
 * 
 *
 */
package fr.uge.patchwork.util.xml;