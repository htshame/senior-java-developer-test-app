# Reference Documentation:
This app is designed to calculate search volume score of search keyword.

### What assumptions did I make:
I suppose that the more popular is the keyword, the earlier it will appear in suggested results. If the keyword is 
100% popular, it will appear in suggested results at the first line since the very first character is typed in the search field.

I assumed that API https://completion.amazon.com/search/complete could be replaced by https://completion.amazon.com/api/2017/suggestions

I also assumed that if the search parameter is treated by Amazon as spell corrected or blacklisted, then the score of this keyword should be lowered.

Also I think and Amazon API is pretty fast and reliable, and it's always able to return the response within milliseconds 
even if it has to process hundreds and thousands of request simultaneously.

Other assumptions are not significant to mention them here.

### How does my algorithm work:
It makes two bunches of Amazon API calls: with and without site-variant parameter (API returns different suggestions 
when site-variant is present and when it's not).
To find the appropriate suggestions, user's actions are being emulated: search starts with the first symbol of
the keyword and continues by adding one next character of the keyword at a time until the search parameter will represent itself
the whole keyword.
After that, each API call result is getting processed and the final overall search volume score is calculated as
a sum of scores provided by "the Algorithm". At the end this sum of scores is divided
by two (since there were two bunches of API calls) keywords lengths, multiplied by 100% and rounded to integer.

"The Algorithm" is as follows:
The algorithm is invoked if the exact match of keyword and suggestion is found (character's case is ignored).
At first it calculates the symbolsTypedFactor - the variable based on the number of characters typed before
the keyword match appeared in suggestions list. It's calculated simply as a number of typed
characters divided by overall keyword length.
Secondly it calculates the keywordPositionFactor - the variable based on the position of matched suggestion
in the list of suggestions (I assume that the order of suggestions is indeed significant).
After that it multiplies symbolsTypedFactor and keywordPositionFactor. Result of this multiplication will be
the search volume score.
Lately, it checks whether the search prefix is spell corrected or not and also checks whether the search
prefix is blacklisted or not. If it does, then the appropriate coefficient s are added to the final result.

### Do I think the hint that you gave is correct:
I think it's incorrect, I assume the order of suggestions matters. This assumption is based on my experience and common sense.
It's logical to place more popular result at the top of the suggestions list. The higher suggestion is - the easier it is to find.
After all, response from this API is shown to end users.

### How precise do I think my outcome is and why:
Definitely not precise enough to use it anywhere.
Why:
* I don't think analyzing results of one API is enough. 
I suppose getting search volume score of a keyword is a little bit more complex process :)
* The amount of time given for this task is not enough to provide any adequate result.
* The algorithm is dumb and straightforward.
* `Spell corrected` and `blacklisted` coefficients are random and maybe they aren't even needed.
* All calculations are very rough.
* I doubt that the purpose of this task is the actual acquiring of adequate search volume score,
 it's more like a way of looking at the code a person writes.

## To build:
mvn clean install

## To start:
`mvn spring-boot:run` or navigate to /target directory and run `java -jar search-volume-1.0.jar`
