/**

Copyright (C) SYSTAP, LLC 2006-2007.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Oct 20, 2007
 */

package com.bigdata.rdf.store;

import org.openrdf.model.Value;

import com.bigdata.btree.IIndex;
import com.bigdata.rdf.inf.Justification;
import com.bigdata.rdf.lexicon.ITermIdCodes;
import com.bigdata.rdf.lexicon.LexiconKeyOrder;
import com.bigdata.rdf.lexicon.LexiconRelation;
import com.bigdata.rdf.model.StatementEnum;
import com.bigdata.rdf.model.OptimizedValueFactory._Value;
import com.bigdata.rdf.spo.SPO;
import com.bigdata.rdf.spo.SPOKeyOrder;
import com.bigdata.rdf.spo.SPORelation;
import com.bigdata.relation.accesspath.IAccessPath;
import com.bigdata.relation.accesspath.IChunkedOrderedIterator;
import com.bigdata.relation.accesspath.IElementFilter;
import com.bigdata.relation.accesspath.IKeyOrder;

/**
 * Low-level API directly using long term identifiers rather than an RDF Value
 * object model.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 * 
 * @todo consider refactor making this the primary interface so that we can
 *       realize non-Sesame integrations by layering an appropriate interface
 *       over this one.
 */
public interface IRawTripleStore extends ITripleStore, ITermIdCodes {
    
    /**
     * Value used for a "NULL" term identifier.
     */
    public static final long NULL = 0L;
    
    /**
     * The #of terms in a statement (3 is a triple store, 4 is a quad store).
     * 
     * @todo use this constant throughout and then change over to a quad store.
     */
    public static final int N = 3;

    /**
     * Constant appended to the database namespace to obtain the
     * {@link SPORelation}'s namespace.
     */
    String NAME_SPO_RELATION = ".spo.";
    
    /**
     * Constant appended to the database namespace to obtain the
     * {@link LexiconRelation}'s namespace.
     */
    String NAME_LEXICON_RELATION = ".lexicon.";
    
    /**
     * The name of the index mapping terms to term identifiers.
     * 
     * @todo deprecate by {@link LexiconKeyOrder}
     */
    static final public String name_term2Id = LexiconKeyOrder.TERM2ID.toString();
    
    /**
     * The name of the index mapping term identifiers to terms.
     */
    static final public String name_id2Term = LexiconKeyOrder.ID2TERM.toString();

    /*
     * The names of the various statement indices. 
     */
    
    /**@todo deprecate by {@link SPOKeyOrder}*/
    static final public String name_spo = SPOKeyOrder.SPO.toString();
    static final public String name_pos = SPOKeyOrder.POS.toString();
    static final public String name_osp = SPOKeyOrder.OSP.toString();
    
    /**
     * The name of the optional index in which {@link Justification}s are
     * stored.
     */
    static final public String name_just = "just";

    /** @todo deprecate these methods in favor of {@link LexiconRelation}? */
    abstract public IIndex getTerm2IdIndex();
    abstract public IIndex getId2TermIndex();

    /** @todo deprecate these methods in favor of {@link SPORelation}? */
    abstract public IIndex getSPOIndex();
    abstract public IIndex getPOSIndex();
    abstract public IIndex getOSPIndex();
    
    /**
     * The optional index on which {@link Justification}s are stored.
     */
    abstract public IIndex getJustificationIndex();

    /**
     * Return the statement index identified by the {@link IKeyOrder}.
     * 
     * @param keyOrder
     *            The key order.
     * 
     * @return The statement index for that access path.
     */
    abstract public IIndex getStatementIndex(IKeyOrder<SPO> keyOrder);

    /**
     * Add a term into the term:id index and the id:term index, returning the
     * assigned term identifier (non-batch API).
     * 
     * @param value
     *            The term.
     * 
     * @return The assigned term identifier.
     */
    public long addTerm(Value value);

    /**
     * Batch insert of terms into the database.
     * 
     * @param terms
     *            An array whose elements [0:nterms-1] will be inserted.
     * @param numTerms
     *            The #of terms to insert.
     * 
     * @todo add a Iterator<_Value> getTerms( Value[] terms ) that can be used
     *       to efficiently resolve one or more terms in parallel without having
     *       a side effect in which the terms become defined (the termId remains
     *       NULL if the term is not known). Alternatively add a boolean flag to
     *       this {@link #addTerms(_Value[], int)} indicating whether or not
     *       terms that are not found should be defined. Either approach would
     *       let us reduce the latency for resolving multiple terms without side
     *       effects (note that an LRU term cache is already being used to
     *       reduce latency).
     */
    public void addTerms(_Value[] terms, int numTerms);

    /**
     * Return the RDF {@link Value} given a term identifier (non-batch api).
     * 
     * @return the RDF value or <code>null</code> if there is no term with
     *         that identifier in the index.
     */
    public Value getTerm(long id);

    /**
     * Return the pre-assigned termId for the value (non-batch API).
     * 
     * @param value
     *            Any {@link Value} reference (MAY be <code>null</code>).
     * 
     * @return The pre-assigned termId -or- {@link #NULL} iff the term is not
     *         known to the database.
     */
    public long getTermId(Value value);

    /**
     * Chooses and returns the best {@link IAccessPath} for the given triple
     * pattern.
     * 
     * @param s
     *            The term identifier for the subject -or-
     *            {@link IRawTripleStore#NULL}.
     * @param p
     *            The term identifier for the predicate -or-
     *            {@link IRawTripleStore#NULL}.
     * @param o
     *            The term identifier for the object -or-
     *            {@link IRawTripleStore#NULL}.
     * 
     * @deprecated by {@link SPORelation#getAccessPath(long, long, long)}
     */
    public IAccessPath<SPO> getAccessPath(long s, long p, long o);

    /**
     * Return the {@link IAccessPath} for the specified {@link IKeyOrder} and
     * a fully unbound triple pattern.  This is generally used only when you
     * want to perform a {@link IAccessPath#distinctTermScan()}.
     * 
     * @deprecated by {@link SPORelation#getAccessPath(SPOKeyOrder, com.bigdata.relation.rule.IPredicate)}
     */
    public IAccessPath<SPO> getAccessPath(IKeyOrder<SPO> keyOrder);
    
    /**
     * Return the statement from the database (fully bound s:p:o only).
     * <p>
     * Note: This may be used to examine the {@link StatementEnum}.
     * 
     * @param s
     *            The term identifier for the subject.
     * @param p
     *            The term identifier for the predicate.
     * @param o
     *            The term identifier for the object.
     * 
     * @return The {@link SPO} for that statement, including its
     *         {@link StatementEnum} -or- <code>null</code> iff the statement
     *         is not in the database.
     * 
     * @exception IllegalArgumentException
     *                if any of the arguments is {@link #NULL}.
     */
    public SPO getStatement(long s, long p, long o);
    
    /**
     * Writes the statements onto the statements indices (batch, parallel, NO
     * truth maintenance).
     * 
     * @param stmts
     *            The statements (sorted into {@link IKeyOrder#SPO} order as a
     *            side-effect).
     * 
     * @param numStmts
     *            The #of entries in <i>stmts</i> that are valid.
     * 
     * @return The #of statements that were written on the indices (a statement
     *         that was previously an axiom or inferred and that is converted to
     *         an explicit statement by this method will be reported in this
     *         count as well as any statement that was not pre-existing in the
     *         database).
     */
    public long addStatements(SPO[] stmts, int numStmts );
    
    /**
     * Writes the statements onto the statement indices (batch, parallel, NO
     * truth maintenance).
     * 
     * @param stmts
     *            The statements.
     * 
     * @param numStmts
     *            The #of entries in <i>stmts</i> that are valid.
     * 
     * @param filter
     *            Optional statement filter. Statements matching the filter are
     *            NOT added to the database.
     * 
     * @return The #of statements that were written on the indices (a statement
     *         that was previously an axiom or inferred and that is converted to
     *         an explicit statement by this method will be reported in this
     *         count as well as any statement that was not pre-existing in the
     *         database).
     */
    public long addStatements(SPO[] stmts, int numStmts, IElementFilter<SPO> filter );
    
    /**
     * Writes the statements onto the statement indices (batch, parallel, NO
     * truth maintenance).
     * 
     * @param itr
     *            An iterator visiting the statements to be added.
     * @param filter
     *            Optional statement filter. Statements matching the filter are
     *            NOT added to the database. The iterator is closed by this
     *            operation.
     * 
     * @return The #of statements that were written on the indices (a statement
     *         that was previously an axiom or inferred and that is converted to
     *         an explicit statement by this method will be reported in this
     *         count as well as any statement that was not pre-existing in the
     *         database).
     *         
     * @deprecated by {@link SPORelation#insert(IChunkedOrderedIterator)}
     */
    public long addStatements(IChunkedOrderedIterator<SPO> itr, IElementFilter<SPO> filter);

    /**
     * Removes the statements from the statement indices (batch, parallel, NO
     * truth maintenance).
     * <p>
     * Note: The {@link StatementEnum} on the {@link SPO}s is ignored by this
     * method. It will delete all statements having the same bindings regardless
     * of whether they are inferred, explicit, or axioms.
     * 
     * @param itr
     *            The iterator
     * 
     * @return The #of statements that were removed from the indices.
     */
    public long removeStatements(SPO[] stmts, int numStmts);

    /**
     * Removes the statements from the statement indices (batch, parallel, NO
     * truth maintenance).
     * <p>
     * Note: The {@link StatementEnum} on the {@link SPO}s is ignored by this
     * method. It will delete all statements having the same bindings regardless
     * of whether they are inferred, explicit, or axioms.
     * 
     * @param itr
     *            The iterator
     * 
     * @return The #of statements that were removed from the indices.
     */
    public long removeStatements(IChunkedOrderedIterator<SPO> itr);
    
    /**
     * Filter the supplied set of SPO objects for whether they are "present" or
     * "not present" in the database, depending on the value of the supplied
     * boolean variable (batch API).
     * 
     * @param stmts
     *            the statements to test
     * @param numStmts
     *            the number of statements to test
     * @param present
     *            if true, filter for statements that exist in the db, otherwise
     *            filter for statements that do not exist
     * 
     * @return an iteration over the filtered set of statements
     */
    public IChunkedOrderedIterator<SPO> bulkFilterStatements(SPO[] stmts, int numStmts,
            boolean present);
    
    /**
     * Efficiently filter the supplied set of {@link SPO} objects for whether
     * they are "present" or "not present" in the database, depending on the
     * value of the supplied boolean variable (batch api).
     * 
     * @param itr
     *            an iterator over the set of statements to test
     * @param present
     *            if true, filter for statements that exist in the db, otherwise
     *            filter for statements that do not exist
     * 
     * @return an iteration over the filtered set of statements
     */
    public IChunkedOrderedIterator<SPO> bulkFilterStatements(IChunkedOrderedIterator<SPO> itr, boolean present);
    
    /**
     * This method fills out the statement metadata (type and sid) for SPOs that
     * are present in the database. SPOs not present in the database are left
     * as-is.
     * 
     * @return An iterator visiting the completed {@link SPO}s. Any {@link SPO}s
     *         that were not found will be present but their statement metadata
     *         (type and sid) will be unchanged.
     */
    public IChunkedOrderedIterator<SPO> bulkCompleteStatements(final IChunkedOrderedIterator<SPO> itr);
    
    /**
     * Externalizes a statement using an appreviated syntax.
     */
    public String toString(long s, long p, long o);

    /**
     * Externalizes a term using an abbreviated syntax.
     * 
     * @param termId
     *            The term identifier.
     * 
     * @return A representation of the term.
     */
    public String toString(long termId);

}
