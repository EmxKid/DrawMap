using DrawMap.Domain;
using DrawMap.WebAPI.Services;
using Microsoft.AspNetCore.Mvc;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RouteController : ControllerBase
{
    private readonly IRouteService _routeService;

    public RouteController(IRouteService routeService)
    {
        _routeService = routeService;
    }

    [HttpPost]
    public async Task<ActionResult<DomainRoute>> AddRoute([FromBody] DomainRoute route)
    {
        var created = await _routeService.AddRoute(route);
        return CreatedAtAction(nameof(GetRoute), new { routeId = created.Id }, created);
    }

    [HttpGet("{routeId}")]
    public async Task<ActionResult<DomainRoute>> GetRoute(string routeId)
    {
        var route = await _routeService.GetRoute(routeId);
        if (route is null)
            return NotFound();

        return Ok(route);
    }

    [HttpDelete("{routeId}")]
    public async Task<ActionResult<bool>> DeleteRoute(string routeId)
    {
        var result = await _routeService.DeleteRoute(routeId);
        if (!result)
            return NotFound();

        return Ok(result);
    }

    [HttpPut("{routeId}")]
    public async Task<ActionResult<bool>> UpdateRoute(string routeId, [FromBody] DomainRoute route)
    {
        var result = await _routeService.UpdateRoute(routeId, route);
        if (!result)
            return NotFound();

        return Ok(result);
    }

    [HttpPost("{routeId}/photos")]
    public async Task<ActionResult<Photo>> AddPhoto(string routeId, [FromBody] Photo photo)
    {
        var route = await _routeService.GetRoute(routeId);
        if (route is null)
            return NotFound($"Route '{routeId}' not found.");

        var created = await _routeService.AddPhoto(routeId, photo);
        return Created($"api/route/{routeId}/photos/{created.Id}", created);
    }
}
