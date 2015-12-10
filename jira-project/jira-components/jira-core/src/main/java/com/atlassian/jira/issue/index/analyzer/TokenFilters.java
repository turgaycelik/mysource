package com.atlassian.jira.issue.index.analyzer;

import javax.annotation.Nullable;

import com.atlassian.jira.index.LuceneVersion;

import com.google.common.base.Function;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianStemFilter;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.br.BrazilianStemFilter;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.cz.CzechStemFilter;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanStemFilter;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.el.GreekStemFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.tartarus.snowball.ext.ArmenianStemmer;
import org.tartarus.snowball.ext.BasqueStemmer;
import org.tartarus.snowball.ext.CatalanStemmer;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.RussianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;

import static org.apache.lucene.analysis.StopFilter.makeStopSet;

public class TokenFilters
{
    public static class General
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> none()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(@Nullable TokenStream input)
                    {
                        return input;
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> none()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(@Nullable TokenStream input)
                    {
                        return input;
                    }
                };
            }
        }
    }

    public static class CJK
    {
        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, CJKAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Armenian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new ArmenianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, ArmenianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Basque
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new BasqueStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, BasqueAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Catalan
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new CatalanStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, CatalanAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Danish
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new DanishStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, DanishAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Dutch
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new DutchStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, DutchAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Finnish
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new FinnishStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, FinnishAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Hungarian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new HungarianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, HungarianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Norwegian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new NorwegianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, NorwegianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Romanian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new RomanianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, RomanianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Russian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new RussianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, RussianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Spanish
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new SpanishStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, SpanishAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Swedish
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new SwedishStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, SwedishAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Bulgarian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> standard()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new BulgarianStemFilter(input);
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, BulgarianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Portuguese
    {
        public static class Brazil
        {
            public static class Stemming
            {
                public static Function<TokenStream, TokenStream> standard()
                {
                    return new Function<TokenStream, TokenStream>()
                    {
                        @Override
                        public TokenStream apply(final TokenStream input)
                        {
                            return new BrazilianStemFilter(input);
                        }
                    };
                }
            }

            public static class StopWordRemoval
            {
                public static Function<TokenStream, TokenStream> defaultSet()
                {
                    return new Function<TokenStream, TokenStream>()
                    {
                        @Override
                        public TokenStream apply(final TokenStream input)
                        {
                            return new StopFilter(LuceneVersion.get(), input, BrazilianAnalyzer.getDefaultStopSet());
                        }
                    };
                }
            }
        }

        public static class Portugal
        {
            public static class Stemming
            {
                public static Function<TokenStream, TokenStream> aggressive()
                {
                    return new Function<TokenStream, TokenStream>()
                    {
                        @Override
                        public TokenStream apply(final TokenStream input)
                        {
                            return new SnowballFilter(input, new PortugueseStemmer());
                        }
                    };
                }
            }

            public static class StopWordRemoval
            {
                public static Function<TokenStream, TokenStream> defaultSet()
                {
                    return new Function<TokenStream, TokenStream>()
                    {
                        @Override
                        public TokenStream apply(final TokenStream input)
                        {
                            return new StopFilter(LuceneVersion.get(), input, PortugueseAnalyzer.getDefaultStopSet());
                        }
                    };
                }
            }
        }
    }

    public static class Czech
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> standard()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new CzechStemFilter(input);
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, CzechAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class French
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new FrenchStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, FrenchAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class German
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> standard()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new GermanStemFilter(input);
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, makeStopSet(GermanAnalyzer.GERMAN_STOP_WORDS));
                    }
                };
            }
        }
    }

    public static class Greek
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> standard()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new GreekStemFilter(input);
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, GreekAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class Italian
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> agressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new ItalianStemmer());
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, ItalianAnalyzer.getDefaultStopSet());
                    }
                };
            }
        }
    }

    public static class English
    {
        public static class Stemming
        {
            public static Function<TokenStream, TokenStream> aggressive()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenFilter apply(final TokenStream input)
                    {
                        return new SnowballFilter(input, new EnglishStemmer());
                    }
                };
            }

            public static Function<TokenStream, TokenStream> moderate()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenFilter apply(final TokenStream input)
                    {
                        return new KStemFilter(input);
                    }
                };
            }

            public static Function<TokenStream, TokenStream> minimal()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenFilter apply(final TokenStream input)
                    {
                        return new EnglishMinimalStemFilter(input);
                    }
                };
            }
        }

        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
                    }
                };
            }
        }
    }

    public static class Thai
    {
        public static class StopWordRemoval
        {
            public static Function<TokenStream, TokenStream> defaultSet()
            {
                return new Function<TokenStream, TokenStream>()
                {
                    @Override
                    public TokenStream apply(final TokenStream input)
                    {
                        return new StopFilter(LuceneVersion.get(), input, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
                    }
                };
            }
        }
    }
}
